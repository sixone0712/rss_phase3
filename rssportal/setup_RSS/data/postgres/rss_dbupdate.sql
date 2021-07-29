DO '
begin
    if exists(select schema_name from information_schema.schemata where schema_name = ''rsss'') THEN
        if not exists(select 1 from information_schema.columns where table_schema = ''rsss'' and table_name = ''black_list'') THEN
            -- create black_list
            create table rsss.black_list
            (
                index serial not null, token text not null, expired timestamp not null
            );
            alter table rsss.black_list
                owner to rssadmin;
            create unique index black_list_index_uindex
                on rsss.black_list (index);

            -- add refreshtoken for users
            alter table rsss.users add refreshtoken text;

            -- add plantype for collection_plan
            alter table rsss.collection_plan add column plantype text default ''ftp''::text not null;
            -- add command for collection_plan
            alter table rsss.collection_plan add column command text;
            -- add directory for collection_plan
            alter table rsss.collection_plan add column directory text;

            -- alter type of cinterval from integer to bigint
            alter table rsss.collection_plan alter column cinterval type bigint using cinterval::bigint;

            -- alter column logtype to permit null
            alter table rsss.collection_plan alter column logtype drop not null;
        end if;
    end if;
end;
'  LANGUAGE plpgsql

\q