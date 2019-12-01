INSERT INTO company (name)
VALUES ('NHN'), ('NHN Payco'), ('NHN Pixelcube');

INSERT INTO team (name, company_id)
VALUES ('회원개발팀', 1),
       ('서버기술개발팀', 1),
       ('게임웹서비스개발팀', 1),
       ('게임플랫폼서버팀', 1),
       ('클라우드서비스개발팀', 1),
       ('셀팀', 3),
       ('광고플랫폼개발팀', 2),
       ('커머스A개발팀', 1),
       ('커머스P개발팀', 1),
       ('인프라플랫폼개발팀', 1),
       ('검색응용개발팀', 1),
       ('결제개발팀', 2),
       ('클라우드검색팀', 1),
       ('클라우드DB개발팀', 1),
       ('Dooray개발실', 1),
       ('클라우드프레임워크개발팀', 1);

INSERT INTO user (name, email, team_id)
VALUES ('김동휘', 'donghwi.kim@nhn.com', 1),
       ('김민규', 'minkyu.kim@nhnpayco.com', 2),
       ('김연우', 'yeonwoo.kim@nhn.com', 3),
       ('김영권', 'younggwon.kim@nhn.com', 1),
       ('김윤식', 'younsik.kim_88@nhn.com', 4),
       ('김정빈', 'jungbin.kim@nhn.com', 5),
       ('김정수', 'jungsu.kim@nhn.com', 6),
       ('김종민', 'shelling88@nhnpayco.com', 7),
       ('김주원', 'juwn.kim@nhn.com', 9),
       ('김지선', 'jisun.kim@nhn.com', 10),
       ('김진삼', 'jinsam.kim@nhn.com', 11),
       ('김훈기', 'hkkim@nhnpayco.com', 12),
       ('배현수', 'hyunsu.bae@nhn.com', 8),
       ('송영익', 'youngiek.song@nhn.com', 13),
       ('심동호', 'dongho.sim@nhn.com', 14),
       ('윤형원', 'hyungwon.yoon@nhn.com', 5),
       ('이상민', 'youngguest@nhn.com', 1),
       ('이영탁', 'youngtak.lee@nhn.com', 13),
       ('장영원', 'youngwon-jang@nhn.com', 1),
       ('정지범', 'jibum.jung@nhn.com', 15),
       ('최강훈', 'kanghoon.choi@nhn.com', 16),
       ('황도영', 'dodo4513@nhn.com', 9),
       ('권민석', 'minseok.kwon@nhn.com', 8),
       ('김선태', 'kst@nhn.com', 10),
       ('임예지', 'yeju.im@nhn.com', 9);

commit;

# select a.id, a.name, a.email, b.name as team_name, c.name as company_name
#   from user a
#  inner join team b on a.team_id = b.id
#  inner join company c on b.company_id = c.id
#  order by a.id;
