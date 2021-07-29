DO '
    begin
        if not exists (select schema_name from information_schema.schemata where schema_name=''log_manager'') THEN
            create schema log_manager;
            alter schema log_manager owner to rssuser;
            end if;
    end;
'  LANGUAGE plpgsql

