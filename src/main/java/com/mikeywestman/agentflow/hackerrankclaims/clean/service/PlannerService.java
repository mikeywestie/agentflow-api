package com.mikeywestman.agentflow.hackerrankclaims.clean.service;

import com.mikeywestman.agentflow.hackerrankclaims.clean.model.*;
import com.mikeywestman.agentflow.hackerrankclaims.clean.util.Texts;

import java.util.*;

public class PlannerService {
    public ClaimPlan plan(ClaimRow row) {
        String text = Texts.norm(row.userClaim()).replace('_', ' ');
        String object = Texts.allowedObject(Texts.notBlank(row.claimObject()) ? row.claimObject() : inferObject(text));
        String issue = inferIssue(text, object);
        String part = inferPart(text, object);
        List<String> risks = new ArrayList<>();
        if (Texts.containsAny(text, "ignore all previous", "approve", "skip review", "mark this row supported")) {
            risks.add("text_instruction_present");
        }
        return new ClaimPlan(object, issue, part, risks);
    }

    private String inferObject(String text) {
        if (Texts.containsAny(text, "laptop", "screen", "keyboard", "trackpad", "hinge")) return "laptop";
        if (Texts.containsAny(text, "package", "parcel", "box", "shipping", "delivery")) return "package";
        return "car";
    }

    private String inferIssue(String text, String object) {
        if (Texts.containsAny(text, "shatter", "shattered", "smashed", "broken glass")) return "glass_shatter";
        if (Texts.containsAny(text, "crack", "cracked", "fracture")) return "crack";
        if (Texts.containsAny(text, "scratch", "scrape", "scraped", "scuff", "mark")) return "scratch";
        if (Texts.containsAny(text, "dent", "dented", "ding")) return "dent";
        if (Texts.containsAny(text, "missing", "not inside", "not there", "gone")) return "missing_part";
        if (Texts.containsAny(text, "broken", "snapped", "toot", "not sitting")) return "broken_part";
        if (Texts.containsAny(text, "torn", "tear", "ripped", "phati")) return object.equals("package") ? "torn_packaging" : "broken_part";
        if (Texts.containsAny(text, "crushed", "crush", "collapsed", "caved")) return object.equals("package") ? "crushed_packaging" : "dent";
        if (Texts.containsAny(text, "water", "wet", "soaked", "liquid", "rain")) return "water_damage";
        if (Texts.containsAny(text, "stain", "stained")) return "stain";
        if (Texts.containsAny(text, "damage", "damaged")) return object.equals("package") ? "crushed_packaging" : "broken_part";
        return "unknown";
    }

    private String inferPart(String text, String object) {
        if (object.equals("car")) {
            if (text.contains("front bumper")) return "front_bumper";
            if (Texts.containsAny(text, "rear bumper", "back bumper", "parachoques trasero")) return "rear_bumper";
            if (Texts.containsAny(text, "windshield", "windscreen", "front glass")) return "windshield";
            if (Texts.containsAny(text, "side mirror", "mirror")) return "side_mirror";
            if (text.contains("headlight")) return "headlight";
            if (Texts.containsAny(text, "taillight", "tail light", "rear light")) return "taillight";
            if (text.contains("door")) return "door";
            if (Texts.containsAny(text, "hood", "bonnet")) return "hood";
            if (text.contains("fender")) return "fender";
            return "body";
        }
        if (object.equals("laptop")) {
            if (Texts.containsAny(text, "screen", "display")) return "screen";
            if (Texts.containsAny(text, "keyboard", "keys", "key")) return "keyboard";
            if (Texts.containsAny(text, "trackpad", "touchpad")) return "trackpad";
            if (text.contains("hinge")) return "hinge";
            if (Texts.containsAny(text, "corner", "edge")) return "corner";
            return "body";
        }
        if (text.contains("corner")) return "package_corner";
        if (Texts.containsAny(text, "side", "surface")) return "package_side";
        if (Texts.containsAny(text, "seal", "tape")) return "seal";
        if (text.contains("label")) return "label";
        if (Texts.containsAny(text, "contents", "inside")) return "contents";
        return "box";
    }
}
