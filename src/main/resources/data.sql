MERGE INTO mpa_ratings KEY (id)
VALUES (1, 'G', 'Без ограничений');

MERGE INTO mpa_ratings KEY (id)
VALUES (2, 'PG', 'Детям рекомендуется смотреть с родителями');

MERGE INTO mpa_ratings KEY (id)
VALUES (3, 'PG-13', 'Детям до 13 лет просмотр не желателен');

MERGE INTO mpa_ratings KEY (id)
VALUES (4, 'R', 'Подростки до 17 лет допускаются только с родителями');

MERGE INTO mpa_ratings KEY (id)
VALUES (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');

MERGE INTO genres KEY (id)
VALUES (1, 'Комедия');

MERGE INTO genres KEY (id)
VALUES (2, 'Драма');

MERGE INTO genres KEY (id)
VALUES (3, 'Мультфильм');

MERGE INTO genres KEY (id)
VALUES (4, 'Триллер');

MERGE INTO genres KEY (id)
VALUES (5, 'Документальный');

MERGE INTO genres KEY (id)
VALUES (6, 'Боевик');
