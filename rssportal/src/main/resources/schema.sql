DO '
    begin
        if not exists (select schema_name from information_schema.schemata where schema_name=''rsss'') THEN
            create schema rsss;
            alter schema rsss owner to rssadmin;

            create sequence rsss.seq_permissions
                maxvalue 9999999999;

            alter sequence rsss.seq_permissions owner to rssadmin;

            create sequence rsss.seq_users
                minvalue 10000
                maxvalue 9999999999;

            alter sequence rsss.seq_users owner to rssadmin;

            create sequence rsss.collection_plan_id_seq;

            alter sequence rsss.collection_plan_id_seq owner to rssadmin;

            create sequence rsss.genres_id_seq;

            alter sequence rsss.genres_id_seq owner to rssadmin;

            create table rsss.download_list
            (
                id      serial    not null
                    constraint download_list_pk
                        primary key,
                created timestamp not null,
                status  text default ''new''::text,
                planid  integer   not null,
                path    text      not null,
                title   text      not null,
                machine text default ''''::text,
                fab     text default ''''::text,
                start   timestamp,
                "end"   timestamp
            );


            alter table rsss.download_list
                owner to rssadmin;

            create unique index download_list_id_uindex
                on rsss.download_list (id);

            create table rsss.permissions
                (
                    id integer default nextval(''rsss.seq_permissions''::regclass) not null
                        constraint permissions_pkey
                            primary key, permname varchar(20) not null, validity boolean
                    );

            alter table rsss.permissions
                owner to rssadmin;

            create table rsss.collection_plan
            (
                id             serial                                                 not null
                    constraint collection_plan_pk
                        primary key,
                tool           text                                                   not null,
                logtype        text,
                description    text,
                collecttype    integer   default 1,
                cinterval      bigint    default 0                                    not null,
                ownerid        integer                                                not null,
                stop           boolean   default false,
                tscreated      timestamp default (now())::timestamp without time zone not null,
                tsstart        timestamp                                              not null,
                tsend          timestamp                                              not null,
                tslastcollect  timestamp,
                tsnext         timestamp,
                tslastpoint    timestamp,
                tscollectstart timestamp default now()                                not null,
                laststatus     text      default ''registered''::text                   not null,
                planname       text                                                   not null,
                logtypestr     text,
                fab            text,
                plantype       text      default ''ftp''::text                          not null,
                command        text,
                directory      text,
                lasttimestamp  text,
                separatedzip   boolean   default false
            );

            alter table rsss.collection_plan
                owner to rssadmin;

            create table rsss.cmd
                (
                    id serial not null
                        constraint cmd_pk
                            primary key, cmd_name text not null, cmd_type text not null, created timestamp not null, modified timestamp not null, validity boolean not null
                    );

            alter table rsss.cmd
                owner to rssadmin;

            create unique index cmd_id_uindex
                on rsss.cmd (id);

            create table rsss.genres
                (
                    id integer default nextval(''rsss.genres_id_seq''::regclass) not null
                        constraint genres_pk
                            primary key, name text not null, category text not null, created timestamp not null, modified timestamp not null, validity boolean not null
                    );

            alter table rsss.genres
                owner to rssadmin;

            create unique index genres_id_uindex
                on rsss.genres (id);

            create unique index genres_name_uindex
                on rsss.genres (name);

            create table rsss."genreUpdate"
                (
                    update timestamp not null
                    );

            alter table rsss."genreUpdate"
                owner to rssadmin;

            create table rsss.users
                (
                    id integer default nextval(''rsss.seq_users''::regclass) not null
                        constraint users_pkey
                            primary key, username text not null
                    constraint users_username_key
                        unique, password text not null, created timestamp default now(), modified timestamp, validity boolean default true, permissions text, lastaccess timestamp, refreshtoken text
                    );

            alter table rsss.users
                owner to rssadmin;

            create table rsss.download_history
                (
                    id serial not null, dl_date timestamp default now(), dl_user text, dl_type text, dl_filename text, validity boolean not null, dl_status text
                    );

            alter table rsss.download_history
                owner to rssadmin;

            create unique index download_history_id_uindex
                on rsss.download_history (id);

            create table rsss.black_list
                (
                    index serial not null, token text not null, expired timestamp not null
                    );

            alter table rsss.black_list
                owner to rssadmin;

            create unique index black_list_index_uindex
                on rsss.black_list (index);

            create table rsss.config_history
            (
                idx      serial not null,
                username text   not null,
                filename text   not null,
                type     text   not null,
                date     timestamp default now(),
                validity boolean not null
            );

            alter table rsss.config_history
                owner to rssadmin;

            create unique index config_history_idx_uindex
                on rsss.config_history (idx);

            end if;


            -- DB Migration from 2020 Phase 4 to 2021 Phase1
            if not exists(select column_name from information_schema.columns WHERE table_name = ''download_list'' AND table_schema = ''rsss'' and column_name like ''machine'') then
                alter table rsss."download_list" add column machine text default ''''::text;
                update rsss."download_list" set machine=''ALL'' where machine = '''';
            end if;

            if not exists(select column_name from information_schema.columns where table_name = ''download_list'' and table_schema = ''rsss'' and column_name like ''fab'') then
             alter table rsss."download_list" add column fab text default ''''::text;
            end if;

            if not exists(select column_name from information_schema.columns where table_name = ''download_list'' and table_schema = ''rsss'' and column_name like ''start'') then
                alter table rsss."download_list" add column start timestamp;
            end if;

            if not exists(select column_name from information_schema.columns where table_name = ''download_list'' and table_schema = ''rsss'' and column_name like ''end'') then
                alter table rsss."download_list" add column "end" timestamp;
            end if;

            if not exists(select column_name from information_schema.columns where table_name = ''collection_plan'' AND table_schema = ''rsss'' and column_name like ''separatedzip'') then
                alter table rsss."collection_plan" add column separatedzip boolean default false;
            end if;
    end;
'  LANGUAGE plpgsql

