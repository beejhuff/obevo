//// CHANGE name=chng1
CREATE TABLE TABLE_A (
	A_ID    INT	NOT NULL,
	B_ID INT NULL
)
GO

//// CHANGE FK name=fkChange
ALTER TABLE TABLE_A ADD FOREIGN KEY (B_ID) REFERENCES TABLE_B(B_ID)
GO

//// CHANGE name=index
CREATE INDEX TABLE_A_IND1 ON TABLE_A (A_ID, B_ID)

//// CHANGE name=chng2
ALTER TABLE TABLE_A ADD COLUMN C_ID INT NULL
GO

//// CHANGE name=chng3
ALTER TABLE TABLE_A ADD COLUMN D_ID INT NULL
GO
