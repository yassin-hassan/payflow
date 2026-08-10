create table invoices (
    id uuid primary key,
    amount numeric(10, 2) not null check ( amount > 0 ),
    status varchar(20) not null check ( status in ('PAID', 'PARTIAL', 'PENDING') )
);

create table payments(
    id uuid primary key,
    invoice_id uuid not null references invoices(id),
    event_id varchar(255) not null unique,
    amount numeric(10, 2) not null check ( amount > 0 )
);

