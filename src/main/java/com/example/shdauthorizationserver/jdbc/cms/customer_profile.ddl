CREATE TABLE cms_customer_profiles (
created_by VARCHAR(50) NOT NULL,
updated_by VARCHAR(50),
created_at TIMESTAMP NOT NULL,
updated_at TIMESTAMP,
username VARCHAR(50),
given_name VARCHAR(100) NOT NULL,
family_name VARCHAR(100) NOT NULL,
middle_name VARCHAR(100),
preferred_name VARCHAR(200),
picture VARCHAR(500),
email VARCHAR(254) NOT NULL,
email_verified BOOLEAN NOT NULL,
gender VARCHAR(10),
dob DATE,
zone_info VARCHAR(100),
locale VARCHAR(30),
phone VARCHAR(15),
phone_verified BOOLEAN NOT NULL,
address VARCHAR(500),
sin CHAR(9),
occupation VARCHAR(50),
profile_completed BOOLEAN DEFAULT FALSE NOT NULL
);

ALTER TABLE cms_customer_profiles
    ADD CONSTRAINT cms_customer_profiles_username_pk
        PRIMARY KEY( username );