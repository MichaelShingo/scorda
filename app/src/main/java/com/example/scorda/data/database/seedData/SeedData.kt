package com.example.scorda.data.database.seedData

import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.KeySignature

object SeedData {
    val composers = listOf(
        Composer(firstName = "Johann Sebastian", lastName = "Bach"),
        Composer(firstName = "Ludwig van", lastName = "Beethoven"),
        Composer(firstName = "Ludovico", lastName = "Einaudi"),
        Composer(firstName = "Kensuke", lastName = "Ushio"),
        Composer(firstName = "Franz", lastName = "Schubert"),
        Composer(firstName = "Niccolò", lastName = "Paganini"),
        Composer(firstName = "Fritz", lastName = "Kreisler"),
        Composer(firstName = "Antonio", lastName = "Vivaldi"),
        Composer(firstName = "Wolfgang Amadeus", lastName = "Mozart"),
        Composer(firstName = "Max", lastName = "Bruch"),
        Composer(firstName = "Pablo de", lastName = "Sarasate"),
        Composer(firstName = "Camille", lastName = "Saint-Saëns"),
        Composer(firstName = "Pyotr Ilyich", lastName = "Tchaikovsky"),
        Composer(firstName = "Henryk", lastName = "Wieniawski"),
        Composer(firstName = "Johann", lastName = "Pachelbel"),
        Composer(firstName = "Richard", lastName = "Wagner"),
        Composer(firstName = "Jules", lastName = "Massenet"),
        Composer(firstName = "Vittorio", lastName = "Monti"),
        Composer(firstName = "Joe", lastName = "Hisaishi"),
        Composer(firstName = "John", lastName = "Williams"),
        Composer(firstName = "Ed", lastName = "Sheeran"),
        Composer(firstName = "Elton", lastName = "John"),
        Composer(firstName = "Stevie", lastName = "Wonder"),
        Composer(firstName = "Frank", lastName = "Sinatra"),
        Composer(firstName = "Elvis", lastName = "Presley"),
        Composer(firstName = "Taylor", lastName = "Swift"),
        Composer(firstName = "Yoko", lastName = "Kanno"),
        Composer(firstName = "Hiroyuki", lastName = "Sawano"),
        Composer(firstName = "Bruno", lastName = "Mars"),
        Composer(firstName = "John", lastName = "Legend"),
        Composer(firstName = "Christina", lastName = "Perri"),
        Composer(firstName = "Beyoncé", lastName = "Knowles"),
        Composer(firstName = "Coldplay", lastName = "Coldplay"),
        Composer(firstName = "The Beatles", lastName = "Beatles"),
        Composer(firstName = "Maroon 5", lastName = "Maroon 5"),
        Composer(firstName = "Hans", lastName = "Zimmer"),
        Composer(firstName = "Ennio", lastName = "Morricone"),
        Composer(firstName = "Alan", lastName = "Menken"),
        Composer(firstName = "Justin", lastName = "Hurwitz"),
        Composer(firstName = "Charles", lastName = "Dancla"),
        Composer(firstName = "Pierre", lastName = "Gavinies"),
        Composer(firstName = "Charles-Auguste", lastName = "De Beriot"),
        Composer(firstName = "Jakob", lastName = "Dont"),
        Composer(firstName = "Rodolphe", lastName = "Kreutzer"),
        Composer(firstName = "Yoshihisa", lastName = "Hirano"), // Hunter x Hunter
        Composer(firstName = "", lastName = "TK"),
        Composer(firstName = "Yoshiki", lastName = "Mizuno"),
        Composer(firstName = "Evan", lastName = "Call"), // Frieren
        Composer(firstName = "Kohei", lastName = "Tanaka"), // One Piece
        Composer(firstName = "", lastName = "Revo"), // AoT
        Composer(firstName = "", lastName = "Yoshiki"), // AoT Red Swan
        Composer(firstName = "Bill", lastName = "Withers"),
        Composer(firstName = "Billy", lastName = "Joel"),
        Composer(firstName = "Calum", lastName = "Scott"),
        Composer(firstName = "Camila", lastName = "Cabello"),
        Composer(firstName = "Sufjan", lastName = "Stevens")
    )

    val genres = listOf(
        "Baroque", "Classical", "Romantic", "Modern", "Soundtrack", "Anime", "Etude / Study", "Pop", "Rock", "Jazz", "Wedding"
    ).map { Genre(name = it) }

    val instruments = listOf(
        "Violin", "Piano", "Cello", "Ensemble", "Viola", "Oboe"
    ).map { Instrument(name = it) }

    data class ScoreSeed(
        val title: String,
        val assetName: String,
        val composerLastName: String,
        val key: KeySignature?,
        val genres: List<String>,
        val instruments: List<String>,
        val setlists: List<String>
    )

    val setlists = listOf("Recital 2024", "Practice Routine", "Wedding Gigs", "Anime Favorites", "Pop Songs")

    val scores = listOf(
        // Etudes and Studies
        ScoreSeed("20 Etudes", "14 Dancla 20 Etudes.pdf", "Dancla", null, listOf("Etude / Study"), listOf("Violin"), listOf("Practice Routine")),
        ScoreSeed("24 Matinees", "15 Gavinies - 24 Matinees.pdf", "Gavinies", null, listOf("Etude / Study"), listOf("Violin"), listOf("Practice Routine")),
        ScoreSeed("Ecole Moderne", "18 Wieniawski Ecole Moderne.pdf", "Wieniawski", null, listOf("Etude / Study"), listOf("Violin"), listOf("Practice Routine")),
        ScoreSeed("24 Caprices", "19 Paganini 24 Caprices.pdf", "Paganini", null, listOf("Romantic", "Etude / Study"), listOf("Violin"), listOf("Practice Routine")),
        ScoreSeed("40 Etudes", "Kreutzer - 40 Etudes.pdf", "Kreutzer", null, listOf("Etude / Study"), listOf("Violin"), listOf("Practice Routine")),

        // Classical / Romantic
        ScoreSeed("Sonatas and Partitas", "Bach - Sonatas and Partitas.pdf", "Bach", KeySignature.G_NATURAL_MINOR, listOf("Baroque"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Romance in F major", "Beethoven - Romance in F major.pdf", "Beethoven", KeySignature.F_NATURAL_MAJOR, listOf("Classical"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Romance in G Major", "Beethoven - Romance in G Major.pdf", "Beethoven", KeySignature.G_NATURAL_MAJOR, listOf("Classical"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Violin Concerto in A major", "Mozart - Violin Concerto in A major.pdf", "Mozart", KeySignature.A_NATURAL_MAJOR, listOf("Classical"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Violin Concerto in G Major", "Mozart - Violin Concerto in G Major.pdf", "Mozart", KeySignature.G_NATURAL_MAJOR, listOf("Classical"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Autumn", "Vivaldi - Autumn.pdf", "Vivaldi", null, listOf("Baroque"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Spring", "Vivaldi - Spring.pdf", "Vivaldi", KeySignature.E_NATURAL_MAJOR, listOf("Baroque"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Summer", "Vivaldi - Summer.pdf", "Vivaldi", KeySignature.G_NATURAL_MINOR, listOf("Baroque"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Winter", "Vivaldi - Winter.pdf", "Vivaldi", KeySignature.F_NATURAL_MINOR, listOf("Baroque"), listOf("Violin"), listOf("Recital 2024")),
        ScoreSeed("Canon in D", "Pachelbel - Canon in D.pdf", "Pachelbel", KeySignature.D_NATURAL_MAJOR, listOf("Baroque", "Wedding"), listOf("Violin", "Ensemble"), listOf("Wedding Gigs")),
        ScoreSeed("Meditation from Thais", "Massenet - Meditation from Thais.pdf", "Massenet", KeySignature.D_NATURAL_MAJOR, listOf("Romantic"), listOf("Violin", "Piano"), listOf("Recital 2024", "Wedding Gigs")),
        ScoreSeed("Czardas", "Monti - Czardas.pdf", "Monti", null, listOf("Romantic"), listOf("Violin", "Piano"), listOf("Recital 2024")),
        ScoreSeed("Ave Maria", "Schubert - Wilhelmj - Ave Maria.pdf", "Schubert", null, listOf("Romantic", "Wedding"), listOf("Violin", "Piano"), listOf("Wedding Gigs")),

        // Modern / Einaudi
        ScoreSeed("Experience", "Einaudi - Experience.pdf", "Einaudi", null, listOf("Modern"), listOf("Piano"), listOf("Practice Routine")),
        ScoreSeed("I Giorni", "Einaudi - I Giorni.pdf", "Einaudi", null, listOf("Modern"), listOf("Piano"), listOf("Practice Routine")),
        ScoreSeed("Stella del Mattino", "Einaudi - Stella del Mattino.pdf", "Einaudi", null, listOf("Modern"), listOf("Piano"), listOf("Practice Routine")),

        // Anime / Soundtracks
        ScoreSeed("Merry Go Round of Life", "Howl's Moving Castle - Merry Go Round of Life - Full Score.pdf", "Hisaishi", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("One Summer Day", "Spirited Away - One Summer Day - for solo violin - Full Score.pdf", "Hisaishi", null, listOf("Anime", "Soundtrack"), listOf("Violin"), listOf("Anime Favorites")),
        ScoreSeed("Town with An Ocean View", "Kiki's Delivery Service - Town with An Ocean View - for solo violin.pdf", "Hisaishi", null, listOf("Anime", "Soundtrack"), listOf("Violin"), listOf("Anime Favorites")),
        ScoreSeed("Iris Out", "Chainsaw Man - Iris Out - Full Score.pdf", "Ushio", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Vogel im Kafig", "Attack on Titan - Vogel im Kafig.pdf", "Sawano", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Tank!", "Cowboy Bebop - Tank for Violin and Piano - Full Score.pdf", "Kanno", null, listOf("Anime", "Soundtrack", "Jazz"), listOf("Violin", "Piano"), listOf("Anime Favorites")),
        ScoreSeed("Unravel", "Tokyo Ghoul - Unravel - Full Score.pdf", "TK", null, listOf("Anime", "Rock"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Bluebird", "Naruto OP3 - Bluebird - Full Score.pdf", "Mizuno", null, listOf("Anime"), listOf("Ensemble"), listOf("Anime Favorites")),

        // Pop / Rock
        ScoreSeed("Perfect", "Ed Sheeran - Perfect - Violin and Chords.pdf", "Sheeran", KeySignature.A_NATURAL_MAJOR, listOf("Pop", "Wedding"), listOf("Violin"), listOf("Wedding Gigs", "Pop Songs")),
        ScoreSeed("Thinking Out Loud", "Ed Sheeran - Thinking Out Loud - Full Score.pdf", "Sheeran", KeySignature.D_NATURAL_MAJOR, listOf("Pop", "Wedding"), listOf("Ensemble"), listOf("Wedding Gigs", "Pop Songs")),
        ScoreSeed("Can You Feel the Love Tonight", "Elton John - Can You Feel the Love Tonight.pdf", "John", null, listOf("Pop", "Soundtrack"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("Your Song", "Elton John - Your Song.pdf", "John", null, listOf("Pop"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("Isn't She Lovely", "Stevie Wonder - Isn't She Lovely.pdf", "Wonder", null, listOf("Pop", "Jazz"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("Superstition", "Stevie Wonder - Superstition.pdf", "Wonder", null, listOf("Pop", "Rock"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("Fly Me to the Moon", "Frank Sinatra - Fly Me to the Moon.pdf", "Sinatra", null, listOf("Jazz"), listOf("Ensemble"), listOf("Wedding Gigs", "Pop Songs")),
        ScoreSeed("Can't Help Falling in Love", "Elvis - Can't Help Falling in Love - Full Score.pdf", "Presley", null, listOf("Pop", "Wedding"), listOf("Ensemble"), listOf("Wedding Gigs")),
        ScoreSeed("Love Story", "Taylor Swift - Love Story.pdf", "Swift", null, listOf("Pop"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("Marry You", "Bruno Mars - Marry You - Full Score Violin and Chords.pdf", "Mars", null, listOf("Pop", "Wedding"), listOf("Violin"), listOf("Wedding Gigs")),
        ScoreSeed("A Thousand Years", "Christina Perri - A Thousand Years Violin Part - Full Score.pdf", "Perri", null, listOf("Pop", "Wedding"), listOf("Violin"), listOf("Wedding Gigs")),
        ScoreSeed("All of Me", "John Legend - All of me - Transposed - Violin.pdf", "Legend", null, listOf("Pop", "Wedding"), listOf("Violin"), listOf("Wedding Gigs")),
        ScoreSeed("Halo", "Beyonce - Halo.pdf", "Knowles", null, listOf("Pop"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("Memories", "Maroon 5 - Memories for violin.pdf", "Maroon 5", null, listOf("Pop"), listOf("Violin"), listOf("Pop Songs")),
        ScoreSeed("A Sky Full of Stars", "ColdPlay - A Sky Full of Stars for Violin - Lower octave.pdf", "Coldplay", null, listOf("Pop", "Rock"), listOf("Violin"), listOf("Pop Songs")),
        ScoreSeed("All You Need is Love", "Beatles - All You Need is Love.pdf", "Beatles", null, listOf("Pop", "Rock"), listOf("Ensemble"), listOf("Wedding Gigs")),

        // Hunter x Hunter
        ScoreSeed("Departure", "Hunter x Hunter - Departure - Full Score.pdf", "Hirano", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Kingdom of Predators", "Hunter x Hunter - Kingdom of Predators - Full Score.pdf", "Hirano", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Legend of the Martial Artist", "Hunter x Hunter - Legend of the Martial Artist - Full Score.pdf", "Hirano", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Zoldyck Family Theme", "Hunter x Hunter - Zoldyck Family Theme - Full Score.pdf", "Hirano", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Hegemony of the Food Chain", "Hunter x Hunter - Hegemony of the Food Chain - Full Score.pdf", "Hirano", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),

        // Frieren
        ScoreSeed("Dragon Smasher", "Frieren - Dragon Smasher - Full Score.pdf", "Call", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Time Flows Ever Onward", "Frieren - Time Flows Ever Onward - Full Score.pdf", "Call", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Waltz for Stark and Fern", "Frieren - Waltz for Stark and Fern - Full Score.pdf", "Call", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Zoltraak", "Frieren - Zoltraak - Full Score.pdf", "Call", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),

        // Stevie Wonder
        ScoreSeed("Don't You Worry Bout A Thing", "Stevie Wonder - Don't You Worry Bout A Thing.pdf", "Wonder", null, listOf("Pop", "Jazz"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("I Just Called to Say I Love You", "Stevie Wonder - I Just Called to Say I Love You.pdf", "Wonder", null, listOf("Pop"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("My Cherie Amour", "Stevie Wonder - My Cherie Amour.pdf", "Wonder", null, listOf("Pop", "Jazz"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("Part Time Lover", "Stevie Wonder - Part Time Lover.pdf", "Wonder", null, listOf("Pop"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("Ribbon in the Sky", "Stevie Wonder - Ribbon in the Sky.pdf", "Wonder", null, listOf("Pop"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("Sir Duke", "Stevie Wonder - Sir Duke.pdf", "Wonder", null, listOf("Pop", "Jazz"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("You Are the Sunshine of My Life", "Stevie Wonder - You Are the Sunshine of My Life.pdf", "Wonder", null, listOf("Pop"), listOf("Ensemble"), listOf("Pop Songs")),

        // Kreisler
        ScoreSeed("Liebesfreud", "Kreisler - Liebesfreud.pdf", "Kreisler", KeySignature.C_NATURAL_MAJOR, listOf("Romantic"), listOf("Violin", "Piano"), listOf("Recital 2024")),
        ScoreSeed("Praeludium and Allegro", "Kreisler - Praeludium and Allegro.pdf", "Kreisler", null, listOf("Romantic"), listOf("Violin", "Piano"), listOf("Recital 2024")),
        ScoreSeed("Syncopation", "Kreisler - Syncopation.pdf", "Kreisler", null, listOf("Modern", "Jazz"), listOf("Violin", "Piano"), listOf("Recital 2024")),

        // More Soundtracks
        ScoreSeed("Gladiator", "Gladiator - Full Score.pdf", "Zimmer", null, listOf("Soundtrack"), listOf("Ensemble"), listOf("Recital 2024")),
        ScoreSeed("Imperial March", "Imperial March - Darth Vader Theme.pdf", "Williams", KeySignature.G_NATURAL_MINOR, listOf("Soundtrack"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("Deborah's Theme", "Deborah's Theme - Full Score.pdf", "Morricone", null, listOf("Soundtrack"), listOf("Ensemble"), listOf("Wedding Gigs")),

        // One Piece
        ScoreSeed("Can't Escape", "One Piece - Can't Escape - Full Score.pdf", "Tanaka", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Gold and Oden", "One Piece - Gold and Oden - for solo violin - Full Score.pdf", "Tanaka", null, listOf("Anime", "Soundtrack"), listOf("Violin"), listOf("Anime Favorites")),
        ScoreSeed("To the Grand Line", "One Piece - To the Grand Line - Full Score.pdf", "Tanaka", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Bink no Sake", "One Piece OST - Bink no Sake - Full Score.pdf", "Tanaka", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Luffy's Fierce Attack", "One Piece OST - Luffy's Fierce Attack - Full Score.pdf", "Tanaka", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Overtaken", "One Piece OST - Overtaken - Full Score.pdf", "Tanaka", null, listOf("Anime", "Soundtrack"), listOf("Ensemble"), listOf("Anime Favorites")),

        // Attack on Titan
        ScoreSeed("Jiyuu no Tsubasa", "Shingeki no Kyojin - Jiyuu no Tsubasa - Full Score.pdf", "Revo", null, listOf("Anime", "Rock"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("Red Swan", "Shingeki no Kyojin - Red Swan - for violin and oboe - Full Score.pdf", "Yoshiki", null, listOf("Anime", "Pop"), listOf("Violin", "Oboe"), listOf("Anime Favorites")),
        ScoreSeed("Shinzou wo Sasageyou", "Shingeki no Kyojin Season 2 OP - Shinzou wo Sasageyou - Full Score.pdf", "Revo", null, listOf("Anime", "Rock"), listOf("Ensemble"), listOf("Anime Favorites")),
        ScoreSeed("My War", "Shingeki no Kyojin Season 4 OP - My War - Full Score.pdf", "Revo", null, listOf("Anime", "Rock"), listOf("Ensemble"), listOf("Anime Favorites")),

        // Misc Pop
        ScoreSeed("Tenerife Sea", "Ed Sheeran - Tenerife Sea.pdf", "Sheeran", null, listOf("Pop"), listOf("Piano"), listOf("Pop Songs")),
        ScoreSeed("Lovely Day", "Bill Withers - Lovely Day.pdf", "Withers", null, listOf("Pop", "Jazz"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("She's Always a Woman", "Billy Joel - She's Always a Woman to Me - solo violin.pdf", "Joel", null, listOf("Pop"), listOf("Violin"), listOf("Pop Songs")),
        ScoreSeed("You Are The Reason", "Calum Scott - You Are The Reason - Full Score.pdf", "Scott", null, listOf("Pop", "Wedding"), listOf("Ensemble"), listOf("Wedding Gigs")),
        ScoreSeed("Senorita", "Camila Cabello - Senorita.pdf", "Cabello", null, listOf("Pop"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("Mystery of Love", "Sufjan Stevens - Mystery of Love.pdf", "Stevens", null, listOf("Soundtrack", "Pop"), listOf("Ensemble"), listOf("Pop Songs")),
        ScoreSeed("Visions of Gideon", "Sufjan Stevens - Visions of Gideon.pdf", "Stevens", null, listOf("Soundtrack", "Pop"), listOf("Ensemble"), listOf("Pop Songs"))
    )
}
