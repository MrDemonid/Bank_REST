create database if not exists bank_db;
create user if not exists 'admin'@'%' identified by 'admin';
create user if not exists 'admin'@'localhost' identified by 'admin';
grant all privileges on bank_db.* to 'admin'@'%';
grant all privileges on bank_db.* to 'admin'@'localhost';
flush privileges;
