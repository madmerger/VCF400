package com.vcf400.web.ui;

import java.util.Map;

import com.vcf400.service.RpgMessages;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class UiMessageTranslator {

    private final MessageSource messages;

    private final Map<String, String> keys = Map.ofEntries(
            Map.entry(RpgMessages.VOTED, "ui.rpg.voted"),
            Map.entry(RpgMessages.BADGE, "ui.rpg.badge"),
            Map.entry(RpgMessages.EXHIBIT, "ui.rpg.exhibit"),
            Map.entry(RpgMessages.AWARD, "ui.rpg.award"),
            Map.entry(RpgMessages.EXISTS, "ui.rpg.exists"),
            Map.entry(RpgMessages.ENTER, "ui.rpg.enter"),
            Map.entry(RpgMessages.INELIGIBLE, "ui.rpg.ineligible"),
            Map.entry(RpgMessages.NO_EXHIBIT, "ui.rpg.noExhibit"),
            Map.entry(RpgMessages.NO_AWARD, "ui.rpg.noAward"),
            Map.entry(RpgMessages.NO_COMMENT, "ui.rpg.noComment"),
            Map.entry(RpgMessages.EXHIBIT_NOT_FOUND, "ui.rpg.exhibitNotFound"),
            Map.entry(RpgMessages.NO_NAME.trim(), "ui.rpg.noName"),
            Map.entry(RpgMessages.THANKS, "ui.rpg.thanks"),
            Map.entry(RpgMessages.COMMENT_ID, "ui.rpg.commentId"),
            Map.entry(RpgMessages.HIDDEN_NAME, "ui.rpg.hiddenName"),
            Map.entry(RpgMessages.HIDDEN_COMMENT, "ui.rpg.hiddenComment"),
            Map.entry(RpgMessages.PRIVATE_COMMENT.trim(), "ui.rpg.privateComment"),
            Map.entry(RpgMessages.STATUS_UPDATED, "ui.rpg.statusUpdated"),
            Map.entry(RpgMessages.INVALID_YN, "ui.rpg.invalidYn"),
            Map.entry(RpgMessages.USER_BLANK.trim(), "ui.rpg.userBlank"),
            Map.entry(RpgMessages.USER_NOT_FOUND.trim(), "ui.rpg.userNotFound"),
            Map.entry(RpgMessages.USER_EXISTS, "ui.rpg.userExists"),
            Map.entry(RpgMessages.USER_CREATED, "ui.rpg.userCreated"),
            Map.entry(RpgMessages.NOT_DELETED, "ui.rpg.notDeleted"),
            Map.entry(RpgMessages.DELETED, "ui.rpg.deleted"),
            Map.entry(RpgMessages.END, "ui.rpg.end"),
            Map.entry("VCF/400 - Credits and Copyright", "ui.static.credits"),
            Map.entry(
                    "VCF/400 V1R0 - Copyright (c) 2023--2024 The Little Beige Box",
                    "ui.static.credits.text"),
            Map.entry(
                    "RIGHT CTRL(=ENTER) to continue.",
                    "ui.static.help.text"));

    public UiMessageTranslator(MessageSource messages) {
        this.messages = messages;
    }

    public String tr(String value) {
        if (value == null) {
            return null;
        }
        String key = keys.get(value.trim());
        if (key == null) {
            return value;
        }
        return messages.getMessage(key, null, value.trim(), java.util.Locale.getDefault());
    }
}
