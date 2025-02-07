SET DATABASE DEFAULT TABLE TYPE CACHED;

DROP SEQUENCE PUBLIC.sqUser;
DROP SEQUENCE PUBLIC.sqProduct;
DROP SEQUENCE PUBLIC.sqPurchase;
DROP SEQUENCE PUBLIC.sqPurchaseItem;

DROP TABLE PUBLIC.PURCHASEITEM;
DROP TABLE PUBLIC.PRODUCT;
DROP TABLE PUBLIC.PURCHASE;
DROP TABLE PUBLIC.USER;
DROP TABLE PUBLIC.DUAL;

CREATE
    TABLE PUBLIC.DUAL
    (
        DUMMY CHAR(1) NOT NULL,
        PRIMARY KEY (DUMMY)
    );

INSERT INTO DUAL(DUMMY) VALUES ('X');

CREATE
    TABLE PUBLIC.USER
    (
        ID BIGINT NOT NULL,
        USERNAME VARCHAR(255) NOT NULL,
        PASSWORD CHAR(16) NOT NULL,
        NAME VARCHAR(255) NOT NULL,
        PRIMARY KEY (id)
    );

CREATE UNIQUE INDEX ixUserNameUnique ON USER (NAME ASC);

CREATE 
	TABLE PUBLIC.PRODUCT 
	(
		ID BIGINT NOT NULL, 
		NAME VARCHAR_IGNORECASE NOT NULL, 
		PRICE NUMERIC NOT NULL, 
		DESCRIPTION BINARY NOT NULL, 
		IMAGE BINARY, 
		PRIMARY KEY (ID)
	);

CREATE
	TABLE PUBLIC.PURCHASE 
	(
		ID BIGINT NOT NULL,
		USERID BIGINT NOT NULL,
		BUYDATE DATE NOT NULL,
		PRIMARY KEY (ID),
		CONSTRAINT fkPurchase2User FOREIGN KEY (USERID) REFERENCES PUBLIC.USER (ID)
	);

CREATE
    TABLE PUBLIC.PURCHASEITEM
    (
        ID BIGINT NOT NULL,
        PURCHASEID BIGINT NOT NULL,
        PRODUCTID BIGINT NOT NULL,
        AMOUNT INTEGER NOT NULL,
        PRICE NUMERIC NOT NULL,
        PRIMARY KEY (id),
        CONSTRAINT fkPurchaseItem2Purchase FOREIGN KEY (PURCHASEID) REFERENCES PUBLIC.PURCHASE (ID),
        CONSTRAINT fkPurchaseItem2Product FOREIGN KEY (PRODUCTID) REFERENCES PUBLIC.PRODUCT (ID)
    );
    
CREATE SEQUENCE PUBLIC.sqUser AS BIGINT START WITH 0 INCREMENT BY 1;
CREATE SEQUENCE PUBLIC.sqProduct AS BIGINT START WITH 0 INCREMENT BY 1;
CREATE SEQUENCE PUBLIC.sqPurchase AS BIGINT START WITH 0 INCREMENT BY 1;
CREATE SEQUENCE PUBLIC.sqPurchaseItem AS BIGINT START WITH 0 INCREMENT BY 1;