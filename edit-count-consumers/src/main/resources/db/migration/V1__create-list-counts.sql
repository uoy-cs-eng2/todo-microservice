create sequence hibernate_sequence;

create table edit_count (
    id bigint primary key not null,
    list_id bigint unique not null,
    edit_count bigint default 0 not null
);
