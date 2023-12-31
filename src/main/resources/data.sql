INSERT INTO roles(ID, TYPE)
VALUES (1, 'ROLE_MEMBER'),
       (2, 'ROLE_STORE'),
       (3, 'ROLE_ADMIN');

-- ADMIN 데이터

INSERT INTO addresses(ID, CREATED_AT, UPDATED_AT, NAME, X_COORDINATE, Y_COORDINATE)
VALUES (5000000, '2023-11-13 09:56:05.011788', '2023-11-13 09:56:05.011788',
        '프로그래머스 강남', 37.4974495848055, 127.028422526103);

INSERT INTO members(ID, CREATED_AT, UPDATED_AT,
                    IMAGE, NICK_NAME, PHONE_NUMBER,
                    PROVIDER, PROVIDER_ID, REAL_NAME, ADDRESS_ID)
VALUES (5000000, '2023-11-13 09:56:05.011788', '2023-11-13 09:56:05.011788',
        'https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/member-default-image.png', '요송송', '01012341234',
        'kakao', 3140255994, '킹왕짱 관리자 바로 나 예성', 5000000);

INSERT INTO member_role(ID, MEMBER_ID, ROLE_ID)
VALUES (5000000, 5000000, 3);

-- MEMBER 데이터

INSERT INTO addresses(ID, CREATED_AT, UPDATED_AT, NAME, X_COORDINATE, Y_COORDINATE)
VALUES (5000001, '2023-11-13 09:56:05.011788', '2023-11-13 09:56:05.011788',
        '프로그래머스 강남', 37.4974495848055, 127.028422526103);

INSERT INTO members(ID, CREATED_AT, UPDATED_AT,
                    IMAGE, NICK_NAME, PHONE_NUMBER,
                    PROVIDER, PROVIDER_ID, REAL_NAME, ADDRESS_ID)
VALUES (5000001, '2023-11-13 09:56:05.011788', '2023-11-13 09:56:05.011788',
        'https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/member-default-image.png', '맛있는거만 밝히는 사용자1',
        '01012341234',
        'kakao', 5000001, '테스트 사용자', 5000001);

INSERT INTO member_role(ID, MEMBER_ID, ROLE_ID)
VALUES (5000001, 5000001, 1);

-- STORE 데이터

INSERT INTO addresses(ID, CREATED_AT, UPDATED_AT, NAME, X_COORDINATE, Y_COORDINATE)
VALUES (5000002, '2023-11-13 09:56:05.011788', '2023-11-13 09:56:05.011788',
        '프로그래머스 강남', 37.4974495848055, 127.028422526103);

INSERT INTO members(ID, CREATED_AT, UPDATED_AT,
                    IMAGE, NICK_NAME, PHONE_NUMBER,
                    PROVIDER, PROVIDER_ID, REAL_NAME, ADDRESS_ID)
VALUES (5000002, '2023-11-13 09:56:05.011788', '2023-11-13 09:56:05.011788',
        'https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/member-default-image.png', '맛있는거만 파는 상점주인1',
        '01012341234',
        'kakao', 5000002, '테스트 업체', 5000002);

INSERT INTO member_role(ID, MEMBER_ID, ROLE_ID)
VALUES (5000002, 5000002, 2);

-- ITEM 목록 조회 INDEX
CREATE INDEX idx_store_id_updated_at ON items (store_id, updated_at);
CREATE INDEX idx_store_status ON stores (store_status);
CREATE INDEX idx_coordinates ON addresses (x_coordinate, y_coordinate);
CREATE INDEX idx_address_id ON stores (address_id);
