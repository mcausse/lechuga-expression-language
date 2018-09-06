
drop table pizzas if exists;
     
        
create table pizzas (
	 id_pizza integer,
	 name varchar(100) not null,
	 price numeric(5,2) not null,
	 kind varchar(20) not null
);
