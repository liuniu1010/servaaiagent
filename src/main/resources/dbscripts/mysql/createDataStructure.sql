create table configs(
id                int            not null primary key,
version           int            not null,
configname        varchar(30)    not null,
configvalue       varchar(100),
comments          varchar(100),
unique key        uniqueno_element (configname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

insert into configs(id, version, configname, configvalue) values(1, 1, 'OpenAiApiKey', '<apiKey>');
insert into configs(id, version, configname, configvalue) values(2, 1, 'DateFormat', 'dd/MM/yyyy');
insert into configs(id, version, configname, configvalue) values(3, 1, 'TimeFormat', 'HH:mm:ss');
insert into configs(id, version, configname, configvalue) values(4, 1, 'DateTimeFormat', 'dd/MM/yyyy HH:mm:ss');
insert into configs(id, version, configname, configvalue) values(5, 1, 'GoogleApiKey', '<apiKey>');
insert into configs(id, version, configname, configvalue, comments) values(6, 1, 'AIInstance', 'GoogleAIImpl', 'option of GoogleAIImpl/OpenAIImpl');

create table chatrecord(
id               int             not null primary key,
version          int             not null,
session          varchar(50)     not null,
isrequest        tinyint(1)      not null,
chattime         datetime        not null,
content          text            not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
