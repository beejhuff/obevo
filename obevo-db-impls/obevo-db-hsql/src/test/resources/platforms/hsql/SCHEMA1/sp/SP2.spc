CREATE PROCEDURE SP2 (OUT mycount INT)
READS SQL DATA
BEGIN ATOMIC
    SELECT count(*) into mycount FROM TABLE_A;
END
GO
