create sequence hibernate_sequence;

create table to_do_list (
    id bigint primary key not null,
    name varchar(255) unique not null
);

create table to_do_item (
    id bigint primary key not null,
    list_id bigint not null references to_do_list (id),
    timestamp timestamp not null,
    title varchar(255) not null,
    body text not null
);
