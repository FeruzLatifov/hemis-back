-- =====================================================
-- S042 ROLLBACK: remove the 69 Ordinatura specialities, their two ancestor categories and
--                the 2023 year links of all 71
-- =====================================================
-- Deletes by explicit id, so an ordinatura row a curator created through the UI after this seed
-- survives the rollback — a rollback undoes what the changeset did, not what people did later.
--
-- Four statements, in this order, and the order is load-bearing:
--   1. the year rows (h_speciality_year.speciality_id FKs into h_speciality; ON DELETE CASCADE
--      would cover it, but deleting them explicitly keeps the intent readable),
--   2. the 69 leaves,
--   3. the two ancestor categories — SEPARATELY, and only after the leaves are gone. They cannot
--      go in the same statement: fk_h_speciality_parent is ON DELETE RESTRICT, and a single DELETE
--      evaluates its NOT EXISTS guard against one snapshot, so the L2 category would still look
--      like a parent and survive.
-- And the whole changeset must unwind BEFORE M017 re-narrows chk_h_speciality_edu_type to
-- ('11','12') — Liquibase unwinds in execution order and M017 runs immediately before this seed,
-- so it unwinds immediately after. M017's own rollback names any surviving row if that is broken.
--
-- An attached speciality is NOT deleted: university_speciality_attachment FKs into h_speciality
-- with RESTRICT, so a DELETE would fail with 23503 mid-rollback. Those rows are left in place and
-- named in a NOTICE instead — an attachment is real ministry data and outranks a tidy rollback.
-- The same applies to the ancestors: if a curator has hung a new ordinatura speciality off the
-- cloned category, the childless guard leaves that category standing rather than orphaning it.
-- =====================================================

DO $$
DECLARE
    blocked INTEGER;
BEGIN
    SELECT count(*) INTO blocked
      FROM university_speciality_attachment a
     WHERE a.speciality_id IN (
    '00cf5c05-d065-42ca-b71e-6c57984fcd7a',
    '0248ad1f-6f1d-c35a-3f37-5fd84c713a72',
    '026f7585-f6e5-4dc2-9a6d-a605c9d50359',
    '02a47b89-7ce2-480e-8349-2d56c46e665e',
    '05630311-b725-c23d-a754-3c8d117609fd',
    '057d58a7-7c22-4fba-b09f-97c890e53fa3',
    '0dfe4f19-5cd0-43b1-90c3-362e3804a84e',
    '17d19bb7-fb2b-d37c-298e-565add1527d5',
    '19d8acc7-6250-adfd-6e39-0c1cc2e9966b',
    '1f492904-ce78-9a36-6e0a-5e8706006bb4',
    '234ab11c-391a-223b-3cb5-efedfbf23dbb',
    '25471de9-0883-e51e-6351-09e49800456b',
    '2b56f125-74e8-2292-5b8a-d4063d81f06f',
    '2c529dc8-8a86-0c31-2916-b36c68a1e94a',
    '302dcfa1-4a61-175c-7303-47a05149fa4b',
    '3416f56d-506b-4eed-b912-428bc8f88e25',
    '355ecf60-005e-4900-7e61-fe48d130fdc8',
    '40477745-09c8-ac6d-8c6f-b1e22b76c53f',
    '4486cc8d-bdd8-fc9c-136e-3e7191a98d8e',
    '44aa2483-2136-4c85-b321-2d90102169d6',
    '48af35e2-7eeb-421f-b1af-e8a9f6664265',
    '54b099be-7c60-238e-fab7-63ed14a6888b',
    '55898bc3-2fbd-f0b6-4795-4e55a530abfc',
    '59c80cb8-5946-d5c1-2560-b3dd01c1608c',
    '5ebd720b-4536-4a43-b7dc-fb275971b343',
    '649d72a7-361c-551a-dd5f-6f1462e8b833',
    '673a5360-9e74-23a0-c963-a62031387fb8',
    '6c6443c3-1c9b-3f7e-54bb-d4c6287f1907',
    '6efa73e2-a262-4c30-8661-2c7f3c860644',
    '783ebc94-45cc-3977-3d5a-9675c7a502f4',
    '7ecd715d-b4ce-4c80-9957-0cd7e7f3c118',
    '887635fd-5137-49f8-9887-7c14d88aa48c',
    '88d16660-d024-4b3f-a170-80cb6b6f122d',
    '8c3b1dc7-38ce-4ddf-8621-9a291d2a2a06',
    '8e7c5e46-c7be-475f-b278-eb7ee75b4f90',
    '903a2269-de8e-4e5e-ae83-5d2910438db0',
    '9929aa55-45cb-43a5-b51e-135e46866fc2',
    '9fe017f8-b1ae-11bc-aa9e-6e156a423412',
    'a11ae9c2-d890-402d-93c0-61263955c276',
    'a4f1d775-eb07-37cf-92c1-e0e672d83209',
    'a5da9708-1c4b-40e0-9641-350511b45c92',
    'a7f2ff12-f8dd-1c27-f55b-eae300b10e01',
    'a821f9cb-1939-4362-9103-f5a3020f37cc',
    'b70ea0ed-f3bc-2878-0f70-aa0b43eac237',
    'bac04ea6-708b-4211-80f2-d5f8798e3520',
    'bb1ac3d1-d3ed-c2ca-0d68-6d9707bc1a4d',
    'bf22e55f-ecfa-4415-9921-5e6845c16514',
    'c2beadd7-7c7d-29c0-b469-7a078ea7a112',
    'c876eff8-93dc-4634-bc1d-0a857516d8b2',
    'ca7e1e1e-f07b-c274-a07d-62f56f01a77f',
    'cec4bfa7-bfb3-28bf-91c5-c951eed0c635',
    'd32199eb-19ff-4899-ad1f-ef37cc780e9f',
    'd5e0254d-a8ed-df1d-3f99-14cf8914746e',
    'df5b4ed6-1aa3-4d0a-a828-07acd502f495',
    'df7d962d-84f8-4ba6-9363-bc8287195735',
    'dff37fc0-ce33-6099-a8c2-b2262dfd7209',
    'e4e1572c-0896-4940-b8b9-bb0b64891f5a',
    'e585b5d2-7e1e-4bda-a1b8-a29f2b6bb1e6',
    'e623bb7a-7217-4c4e-a991-a7ec4021dc78',
    'e701da7d-eec3-42a4-a8a8-f77c00a14541',
    'e8ef478b-8fa1-0cad-551a-0759e527f81c',
    'eb8beb28-e91a-4f5b-b06c-5990d127ac41',
    'ee3ff12a-232d-4b7b-a992-6dc64438f418',
    'f279fd6c-d5f1-a1ea-eba5-a53c3c999d87',
    'f40b490d-f1b7-173e-80ce-c540547a1188',
    'f5101799-3c81-4c02-8506-fa45621887ca',
    'f536e2a4-339c-2a16-16ba-eef9d73bdf09',
    'f85beec3-f83d-4b59-939b-a52861a84ab1',
    'fafc7057-0ecc-0294-d5e3-15b4a6e41f40'
    );
    IF blocked > 0 THEN
        RAISE NOTICE 'S042 rollback: % ta ordinatura mutaxassisligi OTM''ga biriktirilgan — o''chirilmaydi', blocked;
    END IF;
END $$;

-- 1. Year links (leaves + the two ancestor categories)
DELETE FROM h_speciality_year
 WHERE year = 2023
   AND speciality_id IN (
    '00cf5c05-d065-42ca-b71e-6c57984fcd7a',
    '0248ad1f-6f1d-c35a-3f37-5fd84c713a72',
    '026f7585-f6e5-4dc2-9a6d-a605c9d50359',
    '02a47b89-7ce2-480e-8349-2d56c46e665e',
    '05630311-b725-c23d-a754-3c8d117609fd',
    '057d58a7-7c22-4fba-b09f-97c890e53fa3',
    '0dfe4f19-5cd0-43b1-90c3-362e3804a84e',
    '17d19bb7-fb2b-d37c-298e-565add1527d5',
    '19d8acc7-6250-adfd-6e39-0c1cc2e9966b',
    '1f492904-ce78-9a36-6e0a-5e8706006bb4',
    '234ab11c-391a-223b-3cb5-efedfbf23dbb',
    '25471de9-0883-e51e-6351-09e49800456b',
    '2b56f125-74e8-2292-5b8a-d4063d81f06f',
    '2c529dc8-8a86-0c31-2916-b36c68a1e94a',
    '302dcfa1-4a61-175c-7303-47a05149fa4b',
    '3416f56d-506b-4eed-b912-428bc8f88e25',
    '355ecf60-005e-4900-7e61-fe48d130fdc8',
    '40477745-09c8-ac6d-8c6f-b1e22b76c53f',
    '4486cc8d-bdd8-fc9c-136e-3e7191a98d8e',
    '44aa2483-2136-4c85-b321-2d90102169d6',
    '48af35e2-7eeb-421f-b1af-e8a9f6664265',
    '54b099be-7c60-238e-fab7-63ed14a6888b',
    '55898bc3-2fbd-f0b6-4795-4e55a530abfc',
    '59c80cb8-5946-d5c1-2560-b3dd01c1608c',
    '5ebd720b-4536-4a43-b7dc-fb275971b343',
    '649d72a7-361c-551a-dd5f-6f1462e8b833',
    '673a5360-9e74-23a0-c963-a62031387fb8',
    '6c6443c3-1c9b-3f7e-54bb-d4c6287f1907',
    '6efa73e2-a262-4c30-8661-2c7f3c860644',
    '783ebc94-45cc-3977-3d5a-9675c7a502f4',
    '7ecd715d-b4ce-4c80-9957-0cd7e7f3c118',
    '887635fd-5137-49f8-9887-7c14d88aa48c',
    '88d16660-d024-4b3f-a170-80cb6b6f122d',
    '8c3b1dc7-38ce-4ddf-8621-9a291d2a2a06',
    '8e7c5e46-c7be-475f-b278-eb7ee75b4f90',
    '903a2269-de8e-4e5e-ae83-5d2910438db0',
    '9929aa55-45cb-43a5-b51e-135e46866fc2',
    '9fe017f8-b1ae-11bc-aa9e-6e156a423412',
    'a11ae9c2-d890-402d-93c0-61263955c276',
    'a4f1d775-eb07-37cf-92c1-e0e672d83209',
    'a5da9708-1c4b-40e0-9641-350511b45c92',
    'a7f2ff12-f8dd-1c27-f55b-eae300b10e01',
    'a821f9cb-1939-4362-9103-f5a3020f37cc',
    'b70ea0ed-f3bc-2878-0f70-aa0b43eac237',
    'bac04ea6-708b-4211-80f2-d5f8798e3520',
    'bb1ac3d1-d3ed-c2ca-0d68-6d9707bc1a4d',
    'bf22e55f-ecfa-4415-9921-5e6845c16514',
    'c2beadd7-7c7d-29c0-b469-7a078ea7a112',
    'c876eff8-93dc-4634-bc1d-0a857516d8b2',
    'ca7e1e1e-f07b-c274-a07d-62f56f01a77f',
    'cec4bfa7-bfb3-28bf-91c5-c951eed0c635',
    'd32199eb-19ff-4899-ad1f-ef37cc780e9f',
    'd5e0254d-a8ed-df1d-3f99-14cf8914746e',
    'df5b4ed6-1aa3-4d0a-a828-07acd502f495',
    'df7d962d-84f8-4ba6-9363-bc8287195735',
    'dff37fc0-ce33-6099-a8c2-b2262dfd7209',
    'e4e1572c-0896-4940-b8b9-bb0b64891f5a',
    'e585b5d2-7e1e-4bda-a1b8-a29f2b6bb1e6',
    'e623bb7a-7217-4c4e-a991-a7ec4021dc78',
    'e701da7d-eec3-42a4-a8a8-f77c00a14541',
    'e8ef478b-8fa1-0cad-551a-0759e527f81c',
    'eb8beb28-e91a-4f5b-b06c-5990d127ac41',
    'ee3ff12a-232d-4b7b-a992-6dc64438f418',
    'f279fd6c-d5f1-a1ea-eba5-a53c3c999d87',
    'f40b490d-f1b7-173e-80ce-c540547a1188',
    'f5101799-3c81-4c02-8506-fa45621887ca',
    'f536e2a4-339c-2a16-16ba-eef9d73bdf09',
    'f85beec3-f83d-4b59-939b-a52861a84ab1',
    'fafc7057-0ecc-0294-d5e3-15b4a6e41f40',
    '64bcaf9e-2bff-5027-beb6-dd92b1d51699',
    '1bb702e2-05f1-5312-b5ff-c084ceb1ef6d'
   );

-- 2. The 69 imported specialities
DELETE FROM h_speciality s
 WHERE s.id IN (
    '00cf5c05-d065-42ca-b71e-6c57984fcd7a',
    '0248ad1f-6f1d-c35a-3f37-5fd84c713a72',
    '026f7585-f6e5-4dc2-9a6d-a605c9d50359',
    '02a47b89-7ce2-480e-8349-2d56c46e665e',
    '05630311-b725-c23d-a754-3c8d117609fd',
    '057d58a7-7c22-4fba-b09f-97c890e53fa3',
    '0dfe4f19-5cd0-43b1-90c3-362e3804a84e',
    '17d19bb7-fb2b-d37c-298e-565add1527d5',
    '19d8acc7-6250-adfd-6e39-0c1cc2e9966b',
    '1f492904-ce78-9a36-6e0a-5e8706006bb4',
    '234ab11c-391a-223b-3cb5-efedfbf23dbb',
    '25471de9-0883-e51e-6351-09e49800456b',
    '2b56f125-74e8-2292-5b8a-d4063d81f06f',
    '2c529dc8-8a86-0c31-2916-b36c68a1e94a',
    '302dcfa1-4a61-175c-7303-47a05149fa4b',
    '3416f56d-506b-4eed-b912-428bc8f88e25',
    '355ecf60-005e-4900-7e61-fe48d130fdc8',
    '40477745-09c8-ac6d-8c6f-b1e22b76c53f',
    '4486cc8d-bdd8-fc9c-136e-3e7191a98d8e',
    '44aa2483-2136-4c85-b321-2d90102169d6',
    '48af35e2-7eeb-421f-b1af-e8a9f6664265',
    '54b099be-7c60-238e-fab7-63ed14a6888b',
    '55898bc3-2fbd-f0b6-4795-4e55a530abfc',
    '59c80cb8-5946-d5c1-2560-b3dd01c1608c',
    '5ebd720b-4536-4a43-b7dc-fb275971b343',
    '649d72a7-361c-551a-dd5f-6f1462e8b833',
    '673a5360-9e74-23a0-c963-a62031387fb8',
    '6c6443c3-1c9b-3f7e-54bb-d4c6287f1907',
    '6efa73e2-a262-4c30-8661-2c7f3c860644',
    '783ebc94-45cc-3977-3d5a-9675c7a502f4',
    '7ecd715d-b4ce-4c80-9957-0cd7e7f3c118',
    '887635fd-5137-49f8-9887-7c14d88aa48c',
    '88d16660-d024-4b3f-a170-80cb6b6f122d',
    '8c3b1dc7-38ce-4ddf-8621-9a291d2a2a06',
    '8e7c5e46-c7be-475f-b278-eb7ee75b4f90',
    '903a2269-de8e-4e5e-ae83-5d2910438db0',
    '9929aa55-45cb-43a5-b51e-135e46866fc2',
    '9fe017f8-b1ae-11bc-aa9e-6e156a423412',
    'a11ae9c2-d890-402d-93c0-61263955c276',
    'a4f1d775-eb07-37cf-92c1-e0e672d83209',
    'a5da9708-1c4b-40e0-9641-350511b45c92',
    'a7f2ff12-f8dd-1c27-f55b-eae300b10e01',
    'a821f9cb-1939-4362-9103-f5a3020f37cc',
    'b70ea0ed-f3bc-2878-0f70-aa0b43eac237',
    'bac04ea6-708b-4211-80f2-d5f8798e3520',
    'bb1ac3d1-d3ed-c2ca-0d68-6d9707bc1a4d',
    'bf22e55f-ecfa-4415-9921-5e6845c16514',
    'c2beadd7-7c7d-29c0-b469-7a078ea7a112',
    'c876eff8-93dc-4634-bc1d-0a857516d8b2',
    'ca7e1e1e-f07b-c274-a07d-62f56f01a77f',
    'cec4bfa7-bfb3-28bf-91c5-c951eed0c635',
    'd32199eb-19ff-4899-ad1f-ef37cc780e9f',
    'd5e0254d-a8ed-df1d-3f99-14cf8914746e',
    'df5b4ed6-1aa3-4d0a-a828-07acd502f495',
    'df7d962d-84f8-4ba6-9363-bc8287195735',
    'dff37fc0-ce33-6099-a8c2-b2262dfd7209',
    'e4e1572c-0896-4940-b8b9-bb0b64891f5a',
    'e585b5d2-7e1e-4bda-a1b8-a29f2b6bb1e6',
    'e623bb7a-7217-4c4e-a991-a7ec4021dc78',
    'e701da7d-eec3-42a4-a8a8-f77c00a14541',
    'e8ef478b-8fa1-0cad-551a-0759e527f81c',
    'eb8beb28-e91a-4f5b-b06c-5990d127ac41',
    'ee3ff12a-232d-4b7b-a992-6dc64438f418',
    'f279fd6c-d5f1-a1ea-eba5-a53c3c999d87',
    'f40b490d-f1b7-173e-80ce-c540547a1188',
    'f5101799-3c81-4c02-8506-fa45621887ca',
    'f536e2a4-339c-2a16-16ba-eef9d73bdf09',
    'f85beec3-f83d-4b59-939b-a52861a84ab1',
    'fafc7057-0ecc-0294-d5e3-15b4a6e41f40'
   )
   AND NOT EXISTS (SELECT 1 FROM university_speciality_attachment a WHERE a.speciality_id = s.id)
   AND NOT EXISTS (SELECT 1 FROM h_speciality c WHERE c.parent_id = s.id);

-- 3. The two cloned ancestor categories — childless only, and only now that the leaves are gone
DELETE FROM h_speciality s
 WHERE s.id IN (
    '64bcaf9e-2bff-5027-beb6-dd92b1d51699',
    '1bb702e2-05f1-5312-b5ff-c084ceb1ef6d'
   )
   AND NOT EXISTS (SELECT 1 FROM university_speciality_attachment a WHERE a.speciality_id = s.id)
   AND NOT EXISTS (SELECT 1 FROM h_speciality c WHERE c.parent_id = s.id);
