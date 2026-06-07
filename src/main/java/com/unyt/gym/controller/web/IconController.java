package com.unyt.gym.controller.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Generates branded PWA icons on the fly using Java2D.
 * Serves /img/icon-192.png and /img/icon-512.png as required by manifest.json.
 *
 * Icon: ink-blue rounded square (#0B1B2E) with copper "F" letter (#B8956A).
 */
@Controller
public class IconController {

    /** Ink-blue background */
    private static final Color INK    = new Color(0x0B, 0x1B, 0x2E);
    /** Copper accent */
    private static final Color ACCENT = new Color(0xB8, 0x95, 0x6A);

    @GetMapping(value = "/img/icon-{size}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] icon(@PathVariable int size) throws Exception {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);

        // Rounded rectangle background
        int radius = size / 5;
        g.setColor(INK);
        g.fill(new RoundRectangle2D.Float(0, 0, size, size, radius, radius));

        // Copper dot decoration (top-right corner accent)
        int dotR = size / 14;
        g.setColor(ACCENT);
        g.fillOval(size - dotR * 3, dotR, dotR * 2, dotR * 2);

        // Letter "F"
        int fontSize = (int) (size * 0.52);
        g.setFont(new Font("Serif", Font.PLAIN, fontSize));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String letter = "F";
        int x = (size - fm.stringWidth(letter)) / 2 - (int)(size * 0.02);
        int y = (size - fm.getHeight()) / 2 + fm.getAscent() - (int)(size * 0.02);
        g.drawString(letter, x, y);

        // Copper underline / accent bar at the bottom
        int barH = Math.max(3, size / 32);
        int barPad = size / 8;
        g.setColor(ACCENT);
        g.setStroke(new BasicStroke(barH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(barPad, size - barPad, size - barPad, size - barPad);

        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return out.toByteArray();
    }
}
