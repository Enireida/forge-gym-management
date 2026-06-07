package com.unyt.gym.service;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.Payment;
import com.unyt.gym.exception.PaymentException;
import com.unyt.gym.pattern.EventBus;
import com.unyt.gym.pattern.PaymentReceiptBuilder;
import com.unyt.gym.pattern.PricingStrategies;
import com.unyt.gym.pattern.PricingStrategy;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Records payments and produces receipts.
 *
 * <p>Demonstrates Strategy + Builder design patterns and the use of the
 * checked {@link PaymentException} (R10).</p>
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          MemberRepository memberRepository) {
        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Charge a member for a plan, applying an optional pricing strategy.
     *
     * @throws PaymentException if the member or amount is invalid
     */
    @Transactional
    public PaymentReceiptBuilder.PaymentReceipt charge(Long memberId,
                                                        BigDecimal basePrice,
                                                        PricingStrategy strategy,
                                                        String method,
                                                        String description)
            throws PaymentException {

        if (basePrice == null || basePrice.signum() <= 0) {
            throw new PaymentException("Invalid amount: " + basePrice);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new PaymentException("Member " + memberId + " not found"));

        PricingStrategy effective = (strategy != null) ? strategy : PricingStrategies.regular();
        BigDecimal finalPrice = effective.apply(basePrice);

        Payment p = new Payment(member, finalPrice, method, description);
        Payment saved = paymentRepository.save(p);

        // R9 — publish a domain event so listeners (audit log, e-mail
        // confirmation) can react. See EventListenerRegistrar for the
        // subscribers of "payment.received".
        EventBus.getInstance().publish("payment.received", saved);
        log.info("Payment recorded: member={} amount={} method={}",
                member.getName(), finalPrice, method);

        return new PaymentReceiptBuilder()
                .member(member.getName())
                .plan(member.getPlan() != null ? member.getPlan().getName() : "")
                .amount(basePrice)
                .discount(basePrice.subtract(finalPrice))
                .method(method)
                .notes(description)
                .build();
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public List<Payment> findByMember(Long memberId) {
        return paymentRepository.findByMemberId(memberId);
    }
}
