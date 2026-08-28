ALTER TABLE app_user
    ADD CONSTRAINT fk_app_user_created_by
        FOREIGN KEY (created_by)
            REFERENCES app_user (app_user_id);

ALTER TABLE app_user
    ADD CONSTRAINT fk_app_user_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES app_user (app_user_id);


ALTER TABLE company
    ADD CONSTRAINT fk_company_created_by
        FOREIGN KEY (created_by)
            REFERENCES app_user (app_user_id);

ALTER TABLE company
    ADD CONSTRAINT fk_company_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES app_user (app_user_id);

CREATE INDEX ix_app_user_created_by
    ON app_user (created_by);

CREATE INDEX ix_app_user_updated_by
    ON app_user (updated_by);

CREATE INDEX ix_company_created_by
    ON company (created_by);

CREATE INDEX ix_company_updated_by
    ON company (updated_by);