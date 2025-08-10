insert into libmod_players (id, name)
values ('00000000-ffff-0000-0000-000000000001', 'RaccoonKing'),
       ('00000000-ffff-0000-0000-000000000002', 'RaccoonFriend'),
       ('00000000-ffff-0000-0000-000000000003', 'OtterGuy')
;
insert into worldmod_region_groups (id, name, priority)
values ('00000000-2000-0000-0000-000000000001', 'spawn_area', 500),
       ('00000000-2000-0000-0000-000000000002', 'raccoon_kingdom', 0)
;
insert into worldmod_region_group_owners (Group_id, owners_id)
values ('00000000-2000-0000-0000-000000000002', '00000000-ffff-0000-0000-000000000001')
;
insert into worldmod_region_group_members (Group_id, members_id)
values ('00000000-2000-0000-0000-000000000002', '00000000-ffff-0000-0000-000000000002')
;
insert into worldmod_regions (id, name, priority, serverName, worldName, claimOwner_id, group_id)
values ('00000000-1000-0000-0000-000000000001', 'spawn', 500, 'smp', 'world', null, '00000000-2000-0000-0000-000000000001'),
       ('00000000-1000-0000-0000-100000000001', 'raccoon_cave', 50, 'smp', 'world', '00000000-ffff-0000-0000-000000000001',
        '00000000-2000-0000-0000-000000000002'),
       ('00000000-1000-0000-0000-100000000002', 'raccoon_hill', 50, 'smp', 'world', '00000000-ffff-0000-0000-000000000002',
        '00000000-2000-0000-0000-000000000002'),
       ('00000000-1000-0000-ffff-000000000001', 'house', 0, 'smp', 'world', '00000000-ffff-0000-0000-000000000002', null),
       ('00000000-1000-0000-ffff-000000000002', 'castle', 0, 'smp', 'world', '00000000-ffff-0000-0000-000000000003', null)
;
insert into worldmod_group_flags (id, flag)
values ('00000000-2000-0000-0000-000000000001', 'manage.chunkload')
;
insert into worldmod_region_flags (id, flag)
values ('00000000-1000-0000-0000-100000000002', 'manage.chunkload')
;
