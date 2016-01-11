package in.divyamary.moviereel;

public enum Genre {
    ACTION("Action"),
    ADVENTURE("Adventure"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    CRIME("Crime"),
    DOCUMENTARY("Documentary"),
    DRAMA("Drama"),
    FAMILY("Family"),
    FANTASY("Fantasy"),
    FOREIGN("Foreign"),
    HISTORY("History"),
    HORROR("Horror"),
    MUSICAL("Music"),
    MYSTERY("Mystery"),
    ROMANCE("Romance"),
    SCI_FI("Science Fiction"),
    TV_MOVIE("TV Movie"),
    THRILLER("Thriller"),
    WAR("War"),
    WESTERN("Western");

    private String name;

    Genre(String name) {
        this.name = name;
    }

    public static Genre getFromName(String genre) {
        for (Genre genreType : Genre.values())
            if (genreType.name.equals(genre))
                return genreType;

        throw new IllegalArgumentException();
    }
}