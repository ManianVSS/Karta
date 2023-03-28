class Example {

    static Random random = new Random()

    static boolean main(String[] args) {
        println("In Main method")
//        return print_message(args)
        print_message(args)
    }

    static boolean print_message(args) {

        if (args != null) {
            println(args)
        } else {
            println("Test")
        }
        return random.nextBoolean()
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    boolean hello_world(String[] args) { return print_message(args) }
}
