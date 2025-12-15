package com.example.secretweapon.rules;

import com.example.secretweapon.model.dto.ExpenseValidationResult;
import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.model.enums.RuleDecisionType;
import com.example.secretweapon.payload.request.ExpenseRequestCreate;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Priority;

import java.math.BigDecimal;

@org.jeasy.rules.annotation.Rule(name = "Dynamic DB Rule", description = "Applies checks based on DB configuration")
public class DynamicExpenseRule {

    private final Rule dbRule; 
    private final int currentRequestCount; //From DB

    public DynamicExpenseRule(Rule dbRule, int currentRequestCount) {
        this.dbRule = dbRule;
        this.currentRequestCount = currentRequestCount;
    }

    // EasyRules sắp xếp thứ tự chạy dựa trên Priority lấy từ DB
    @Priority
    public int getPriority() {
        return dbRule.getPriority() != null ? dbRule.getPriority() : Integer.MAX_VALUE;
    }

    /**
     * @Condition: Trả về TRUE nếu Rule bị vi phạm (để kích hoạt Action)
     * Trả về FALSE nếu mọi thứ OK.
     */
    @Condition
    public boolean when(@Fact("request") ExpenseRequestCreate request) {
        // 1. Check Amount Limit
        if (dbRule.getLimitAmount() != null) {
            if (request.getAmount().compareTo(dbRule.getLimitAmount()) > 0) {
                return true; // Vi phạm Amount
            }
        }

        // 2. Check Frequency Limit
        // Lưu ý: Logic check frequency cần biết user đã tạo bao nhiêu request rồi
        if (dbRule.getLimitCountPerPeriod() != null) {
            if (currentRequestCount > dbRule.getLimitCountPerPeriod()) {
                return true; // Vi phạm Frequency
            }
        }

        return false; // Không vi phạm gì cả
    }

    /**
     * @Action: When @Condition return True
     */
    @Action
    public void then(@Fact("result") ExpenseValidationResult result) {
        // Ghi nhận vi phạm vào result object
        String violationReason = "";
        
        // Xác định lại lý do cho rõ ràng (tùy chọn)
        if (dbRule.getLimitAmount() != null) {
             violationReason = "Exceeded limit amount: " + dbRule.getLimitAmount();
        } else {
             violationReason = "Exceeded request count in period: " + dbRule.getLimitCountPerPeriod();
        }

        // Đánh dấu cần duyệt đặc biệt
        result.flagViolation(
            RuleDecisionType.NEEDS_SPECIAL_APPROVAL, 
            violationReason, 
            dbRule.getId()
        );
    }
}