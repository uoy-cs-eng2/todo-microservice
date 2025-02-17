create table edit_count_by_partition (
    id bigint primary key not null,
    list_id bigint not null,
    partition_id int not null,
    edit_count bigint default 0 not null,
    constraint uk_list_partition unique (list_id, partition_id)
);