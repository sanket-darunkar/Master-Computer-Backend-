-- ============================================================
-- V6: Add installments column to students table
-- Stores multiple fee payment records as a JSON array:
--   [{"amountPaid": 1000, "receiptNumber": "RCP-001", "receiptDate": "2026-10-01"}, ...]
-- ============================================================

ALTER TABLE students
    ADD COLUMN IF NOT EXISTS installments TEXT;

-- Back-fill existing records: if feesPaid / receipt_number / receipt_date exist,
-- migrate them into the first installment entry as a JSON array string.
UPDATE students
SET installments = '[{"amountPaid":"' || COALESCE(fees_paid::TEXT, '') ||
                   '","receiptNumber":"' || COALESCE(receipt_number, '') ||
                   '","receiptDate":"' || COALESCE(receipt_date::TEXT, '') ||
                   '"}]'
WHERE (fees_paid IS NOT NULL OR receipt_number IS NOT NULL OR receipt_date IS NOT NULL)
  AND installments IS NULL;
