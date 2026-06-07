package com.unyt.gym.service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Static food calorie/macro lookup table.
 * Returns int[] { caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g }
 * or null if no match found.
 */
public class FoodCalorieHelper {

    private static final Map<String, int[]> DB = new LinkedHashMap<>();

    static {
        DB.put("chicken breast",  new int[]{165, 31, 0, 3});
        DB.put("gjoks pule",      new int[]{165, 31, 0, 3});
        DB.put("chicken",         new int[]{215, 18, 0, 15});
        DB.put("pule",            new int[]{215, 18, 0, 15});
        DB.put("beef",            new int[]{250, 26, 0, 17});
        DB.put("vici",            new int[]{250, 26, 0, 17});
        DB.put("viçi",            new int[]{250, 26, 0, 17});
        DB.put("salmon",          new int[]{208, 20, 0, 13});
        DB.put("tuna",            new int[]{116, 26, 0, 1});
        DB.put("ton",             new int[]{116, 26, 0, 1});
        DB.put("egg",             new int[]{155, 13, 1, 11});
        DB.put("veze",            new int[]{155, 13, 1, 11});
        DB.put("vezë",            new int[]{155, 13, 1, 11});
        DB.put("milk",            new int[]{61, 3, 5, 3});
        DB.put("qumesht",         new int[]{61, 3, 5, 3});
        DB.put("qumësht",         new int[]{61, 3, 5, 3});
        DB.put("yogurt",          new int[]{59, 10, 4, 0});
        DB.put("kos",             new int[]{59, 10, 4, 0});
        DB.put("rice",            new int[]{130, 3, 28, 0});
        DB.put("oriz",            new int[]{130, 3, 28, 0});
        DB.put("pasta",           new int[]{131, 5, 25, 1});
        DB.put("makarona",        new int[]{131, 5, 25, 1});
        DB.put("bread",           new int[]{265, 9, 49, 3});
        DB.put("buke",            new int[]{265, 9, 49, 3});
        DB.put("bukë",            new int[]{265, 9, 49, 3});
        DB.put("oats",            new int[]{389, 17, 66, 7});
        DB.put("tershere",        new int[]{389, 17, 66, 7});
        DB.put("tërshërë",        new int[]{389, 17, 66, 7});
        DB.put("potato",          new int[]{77, 2, 17, 0});
        DB.put("patate",          new int[]{77, 2, 17, 0});
        DB.put("banana",          new int[]{89, 1, 23, 0});
        DB.put("banane",          new int[]{89, 1, 23, 0});
        DB.put("apple",           new int[]{52, 0, 14, 0});
        DB.put("molle",           new int[]{52, 0, 14, 0});
        DB.put("mollë",           new int[]{52, 0, 14, 0});
        DB.put("molla",           new int[]{52, 0, 14, 0});
        DB.put("orange",          new int[]{47, 1, 12, 0});
        DB.put("portokall",       new int[]{47, 1, 12, 0});
        DB.put("nuts",            new int[]{654, 15, 14, 65});
        DB.put("arra",            new int[]{654, 15, 14, 65});
        DB.put("pizza",           new int[]{266, 11, 33, 10});
        DB.put("burger",          new int[]{295, 17, 24, 14});
        DB.put("chocolate",       new int[]{546, 5, 60, 31});
        DB.put("cokollate",       new int[]{546, 5, 60, 31});
        DB.put("çokollatë",       new int[]{546, 5, 60, 31});
    }

    /**
     * Looks up macros for a food name using case-insensitive substring matching.
     * Returns int[]{caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g} or null.
     */
    public static int[] lookup(String foodName) {
        if (foodName == null || foodName.isBlank()) return null;
        String lower = foodName.toLowerCase().trim();
        // Exact match first
        if (DB.containsKey(lower)) return DB.get(lower);
        // Substring match — find first key contained in input OR input contained in key
        for (Map.Entry<String, int[]> e : DB.entrySet()) {
            if (lower.contains(e.getKey()) || e.getKey().contains(lower)) {
                return e.getValue();
            }
        }
        return null;
    }

    private FoodCalorieHelper() { }
}
