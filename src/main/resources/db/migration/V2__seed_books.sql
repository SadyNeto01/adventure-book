-- The challenge's sample books, fixed to be valid (see DECISIONS.md, "Livros de exemplo").

-- The Crystal Caverns
INSERT INTO book (id, title, author, difficulty) VALUES (1, 'The Crystal Caverns', 'Evelyn Stormrider', 'EASY');
INSERT INTO book_category (book_id, category) VALUES (1, 'ADVENTURE'), (1, 'FANTASY');
INSERT INTO section (book_id, section_id, text, type, options) VALUES
    (1, 1, 'You stand at the entrance of the legendary Crystal Caverns. A cold breeze carries whispers from the darkness below. A broken rope bridge leads into the depths.', 'BEGIN', '[{"description": "Cross the rope bridge carefully", "gotoId": 100}, {"description": "Search the rocky walls for another path", "gotoId": 20}]'::jsonb),
    (1, 20, 'Your hands brush against the cold stone until you find a hidden crevice leading downward. It''s dark, but you hear faint dripping water.', 'NODE', '[{"description": "Enter the crevice", "gotoId": 200, "consequence": {"type": "LOSE_HEALTH", "value": 4, "text": "You scrape your shoulder squeezing through the narrow gap."}}, {"description": "Return to the entrance", "gotoId": 1}]'::jsonb),
    (1, 100, 'The bridge creaks under your weight. Halfway across, a plank snaps beneath your foot!', 'NODE', '[{"description": "Hold on tightly and crawl across", "gotoId": 300}, {"description": "Try to jump to the other side", "gotoId": 400, "consequence": {"type": "LOSE_HEALTH", "value": 7, "text": "You land hard and twist your ankle."}}]'::jsonb),
    (1, 200, 'You emerge into a glittering chamber filled with glowing crystals. At the center lies a pedestal holding an ancient gemstone.', 'NODE', '[{"description": "Take the gemstone", "gotoId": 500}, {"description": "Inspect the walls carefully", "gotoId": 600}]'::jsonb),
    (1, 300, 'You crawl to safety on the other side, breathing heavily. A winding path descends deeper into the cavern.', 'NODE', '[{"description": "Follow the path downward", "gotoId": 600}, {"description": "Rest and recover your strength", "gotoId": 700, "consequence": {"type": "GAIN_HEALTH", "value": 3, "text": "You feel slightly better after resting."}}]'::jsonb),
    (1, 400, 'You barely make the jump, but the rope bridge collapses behind you. There''s no turning back now.', 'NODE', '[{"description": "Continue deeper into the cavern", "gotoId": 600}]'::jsonb),
    (1, 500, 'The moment you touch the gemstone, the chamber begins to shake violently. A secret door opens to reveal a hidden passage.', 'NODE', '[{"description": "Enter the hidden passage", "gotoId": 800}, {"description": "Drop the gemstone and run back", "gotoId": 200}]'::jsonb),
    (1, 600, 'You reach a vast underground lake glowing with bioluminescent crystals. A small boat waits at the shore.', 'NODE', '[{"description": "Take the boat across the lake", "gotoId": 900}, {"description": "Swim across", "gotoId": 1000, "consequence": {"type": "LOSE_HEALTH", "value": 5, "text": "The freezing water chills you to the bone."}}]'::jsonb),
    (1, 700, 'After a short rest, you feel strong enough to continue your journey.', 'NODE', '[{"description": "Head toward the underground lake", "gotoId": 600}]'::jsonb),
    (1, 800, 'The hidden passage leads to a giant crystal throne room, where an ancient spirit awaits.', 'END', NULL),
    (1, 900, 'The boat carries you safely across, but you sense something massive moving in the depths below.', 'NODE', '[{"description": "Ignore it and continue forward", "gotoId": 800}, {"description": "Investigate the movement", "gotoId": 666}]'::jsonb),
    (1, 1000, 'You manage to swim across, shivering but alive. The glowing crystals ahead light your way to a massive door.', 'END', NULL),
    (1, 666, 'Something massive rises from the depths and capsizes your boat. Your adventure ends here.', 'END', NULL);

-- Pirates of the Jade Sea
INSERT INTO book (id, title, author, difficulty) VALUES (2, 'Pirates of the Jade Sea', 'Marina Blackwood', 'MEDIUM');
INSERT INTO book_category (book_id, category) VALUES (2, 'ADVENTURE'), (2, 'FICTION');
INSERT INTO section (book_id, section_id, text, type, options) VALUES
    (2, 1, 'The salty breeze carries the cries of distant gulls as your ship sails across the shimmering Jade Sea. Rumors speak of hidden treasure on the Isle of Serpents.', 'BEGIN', '[{"description": "Take the helm and steer toward the Isle of Serpents", "gotoId": 100}, {"description": "Interrogate the captured pirate in the brig", "gotoId": 20}]'::jsonb),
    (2, 20, 'The captured pirate snarls but whispers about a secret cove guarded by a sea monster.', 'NODE', '[{"description": "Threaten him for more information", "gotoId": 200, "consequence": {"type": "LOSE_HEALTH", "value": 3, "text": "The pirate lunges at you with a hidden dagger before the guards subdue him."}}, {"description": "Leave him and return to the deck", "gotoId": 1}]'::jsonb),
    (2, 100, 'You steer toward the mysterious Isle of Serpents. As you approach, dark storm clouds gather overhead.', 'NODE', '[{"description": "Sail through the storm", "gotoId": 300}, {"description": "Drop anchor and wait for the storm to pass", "gotoId": 400, "consequence": {"type": "LOSE_HEALTH", "value": 5, "text": "A rogue wave crashes onto the deck, injuring several crew members."}}]'::jsonb),
    (2, 200, 'Shaken, the pirate reveals that the sea monster can be lulled to sleep by an enchanted conch shell hidden deep within a wrecked galleon.', 'NODE', '[{"description": "Search for the wrecked galleon", "gotoId": 500}, {"description": "Ignore the legend and continue to the island", "gotoId": 100}]'::jsonb),
    (2, 300, 'The storm rages violently. Lightning flashes illuminate shadowy shapes beneath the waves.', 'NODE', '[{"description": "Dive into the water to investigate", "gotoId": 600, "consequence": {"type": "LOSE_HEALTH", "value": 7, "text": "A swirling current drags you under before the crew hauls you back aboard."}}, {"description": "Stay on deck and brace for impact", "gotoId": 700}]'::jsonb),
    (2, 400, 'After hours of waiting, the storm clears and the Isle of Serpents looms ahead, shrouded in mist.', 'NODE', '[{"description": "Explore the island", "gotoId": 800}]'::jsonb),
    (2, 500, 'You find the wrecked galleon resting on a coral reef. The conch shell glows faintly within a locked chest.', 'NODE', '[{"description": "Unlock the chest carefully", "gotoId": 900}, {"description": "Smash the chest open", "gotoId": 1000, "consequence": {"type": "LOSE_HEALTH", "value": 4, "text": "Splinters cut your hands as you force the chest open."}}]'::jsonb),
    (2, 600, 'Beneath the waves, you glimpse the massive serpent coiled around a treasure-laden wreck.', 'NODE', '[{"description": "Swim closer to investigate", "gotoId": 666}, {"description": "Retreat back to the surface", "gotoId": 300}]'::jsonb),
    (2, 700, 'The storm subsides. Your ship drifts into calm waters, and the sun breaks through the clouds.', 'NODE', '[{"description": "Set a course for the Isle of Serpents", "gotoId": 800}]'::jsonb),
    (2, 800, 'You step onto the white sands of the Isle of Serpents. Ancient ruins rise above the jungle canopy.', 'NODE', '[{"description": "Search the ruins for treasure", "gotoId": 1200}, {"description": "Climb the tallest ruin for a better view", "gotoId": 1300}]'::jsonb),
    (2, 900, 'Inside the chest, you find the enchanted conch shell. Its surface hums softly with power.', 'NODE', '[{"description": "Blow the conch to summon help", "gotoId": 1400}, {"description": "Keep it safe for later", "gotoId": 800}]'::jsonb),
    (2, 1000, 'The chest bursts open, revealing the glowing conch shell.', 'NODE', '[{"description": "Take the shell", "gotoId": 900}]'::jsonb),
    (2, 1200, 'Inside the ruins, you discover a chamber filled with gold and jewels, but a trap triggers!', 'NODE', '[{"description": "Dodge the falling stones", "gotoId": 1500, "consequence": {"type": "LOSE_HEALTH", "value": 6, "text": "A rock grazes your shoulder as you narrowly escape."}}, {"description": "Grab the treasure and run", "gotoId": 1400}]'::jsonb),
    (2, 1300, 'From the top of the ruins, you spot a hidden lagoon surrounded by jagged cliffs.', 'NODE', '[{"description": "Head toward the hidden lagoon", "gotoId": 1400}]'::jsonb),
    (2, 1400, 'You reach a secret pirate hideout filled with treasure. The crew celebrates your discovery.', 'END', NULL),
    (2, 1500, 'Breathing heavily, you escape the collapsing ruins with your life intact.', 'END', NULL),
    (2, 666, 'A whirlpool drags you into darkness. Your adventure ends here.', 'END', NULL);

-- The Prisoner
INSERT INTO book (id, title, author, difficulty) VALUES (3, 'The Prisoner', 'Daniel El Fuego', 'HARD');
INSERT INTO book_category (book_id, category) VALUES (3, 'HORROR'), (3, 'FICTION');
INSERT INTO section (book_id, section_id, text, type, options) VALUES
    (3, 1, 'You wake up in what seems to be a dark prison cell, on an old wooden bed. Metal bars are preventing you to escape from the room. There is no window.', 'BEGIN', '[{"description": "You try to open the door", "gotoId": 500}, {"description": "You look under the bed", "gotoId": 20}]'::jsonb),
    (3, 20, 'You don''t see anything, it''s too dark.', 'NODE', '[{"description": "Try to scan the area with your hands", "gotoId": 30, "consequence": {"type": "LOSE_HEALTH", "value": 6, "text": "As you move your hands left and right under the bed, you cut yourself on a rusty nail."}}]'::jsonb),
    (3, 30, 'You found what seems to be a door key.', 'NODE', '[{"description": "Try to open the door with the key", "gotoId": 1000}]'::jsonb),
    (3, 500, 'The door is locked.', 'NODE', '[{"description": "Gather your thoughts", "gotoId": 1, "consequence": {"type": "LOSE_HEALTH", "value": 3, "text": "You''re getting a little more crazier..."}}]'::jsonb),
    (3, 1000, 'The door opens. You are now free...', 'END', NULL);

-- ids above were inserted by hand: move the identity past them so POST /books doesn't collide
SELECT setval(pg_get_serial_sequence('book', 'id'), (SELECT MAX(id) FROM book));
