//// CHANGE name=chng1
CREATE TABLE TABLE_B (
	B_ID    INT	NOT NULL,
    PRIMARY KEY (B_ID)
)

//// CHANGE name=toRollBackImmediately
ALTER TABLE TABLE_B ADD COLUMN COLABC INT NULL
GO
