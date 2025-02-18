create table alt_edit_count (
  id bigint primary key not null,
  list_id bigint unique not null,
  edit_count bigint default 0 not null
);
