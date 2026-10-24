import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BookService service = new BookService();

        while (true) {
            System.out.println("""
                    
                    ===== МЕНЮ УПРАВЛЕНИЯ =====
                    1. Показать все книги
                    2. Добавить книгу вручную
                    3. Загрузить книги из файла (CSV/TXT)
                    4. Обработать данные через Stream API
                    5. Удалить книгу (демонстрация MyArrayList.remove)
                    0. Выход
                    """);
            System.out.print("Выберите действие: ");
            String input = scanner.nextLine().trim();

            int command;
            try {
                command = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число от 0 до 5.");
                continue;
            }

            switch (command) {
                case 1 -> service.displayAll();
                case 2 -> service.addBookFromConsole(scanner);
                case 3 -> {
                    System.out.print("Введите путь к файлу (например, books.txt): ");
                    String path = scanner.nextLine().trim();
                    service.loadFromFile(path);
                }
                case 4 -> service.processStreamInteractive(scanner);

                case 5 -> service.removeBookByIndex(scanner);
                case 0 -> {
                    System.out.println("Завершение работы программы...");
                    return;
                }
                default -> System.out.println("Неизвестная команда. Попробуйте еще раз.");
            }
        }
    }
}
