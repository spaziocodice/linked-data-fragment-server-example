
drop table if exists role;
drop table if exists contact;
drop table if exists company;

create table company (id int primary key not null,
                           name varchar (200),
                           vat varchar(50));

create table role (id int primary key not null, name varchar(200));

create table contact (id int primary key not null,
                                   surname varchar(200),
                                   name varchar (200),
                                   email varchar(100),
                                   mobile varchar(100),
                                   phone varchar(100),
                                   role_id int not null,
                                   organization_id int not null);

alter table contact
    add constraint role_id_fk
        foreign key (role_id) references role(id);

alter table contact
    add constraint organization_id_fk
        foreign key (organization_id) references company(id);

INSERT INTO role (id, name) VALUES (1, 'Sales Director');
INSERT INTO role (id, name) VALUES (2, 'CEO');
INSERT INTO role (id, name) VALUES (3, 'Customer Service');
INSERT INTO role (id, name) VALUES (4, 'CTO');

INSERT INTO company (id, name, vat) VALUES (1, 'XYZ Ltd', '00238238123');
INSERT INTO company (id, name, vat) VALUES (2, 'ABC Srl', '01648827372');
INSERT INTO company (id, name, vat) VALUES (3, 'JYY Srl', '02627717271');

INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (1, 'Hug', 'Dora', 'd.hug@xyz.com', '+39200232383', '+992392929', 2, 1);
INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (2, 'Smith', 'Ken', 'k.smith@xyz.com', '+39232888318','+999230132', 1, 1);
INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (3, 'Key', 'Lyu', 'l.key@xyz.com', '+39200245351','+9900203823', 3, 1);

INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (4, 'Dotti', 'Lia', 'l.dotti@abc.it', '+393628294745','+3908882382', 2, 2);
INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (5, 'Rossi', 'Giovanni', 'g.rossi@abc.it', '+3987663552', '+39088882992', 4, 2);
INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (6, 'Bianchi', 'Ugo', 'u.bianchi@abc.it', '+393857733645','+39088882765', 1, 1);

INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (7, 'Doe', 'John', 'j.doe@jyy.com', '+2382917337','+9832723728', 2, 3);
INSERT INTO contact (id, surname, name, email, mobile, phone, role_id, organization_id) VALUES (8, 'Hallis', 'Johnatan', 'j.hellis@jyy.com', '+9283829838','+29382938', 3, 3);