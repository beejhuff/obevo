CREATE OR REPLACE PROCEDURE SP2 (IN INVAL INT)

LANGUAGE SQL  DYNAMIC RESULT SETS 1 

BEGIN ATOMIC

    CALL SP3(INVAL);

END
GO