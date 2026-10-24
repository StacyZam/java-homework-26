import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Comparator;
import java.util.Scanner;
import java.util.function.Predicate;

public class BookService {
    private final MyArrayList<Book> bookList = new MyArrayList<>();

    public void addBookDirectly(Book book) {
        bookList.add(book);
    }

    public void addBookFromConsole(Scanner scanner) {
        System.out.println("\n--- Добавление книги вручную ---");

        String title;
        while (true) {
            System.out.print("Введите название книги: ");
            title = scanner.nextLine().trim();
            if (!title.isEmpty()) break;
            System.out.println("Ошибка: название не может быть пустым.");
        }

        String author;
        while (true) {
            System.out.print("Введите автора: ");
            author = scanner.nextLine().trim();
            if (!author.isEmpty()) break;
            System.out.println("Ошибка: автор не может быть пустым.");
        }

        int year;
        int currentYear = Year.now().getValue();
        while (true) {
            System.out.printf("Введите год издания (от 1450 до %d): ", currentYear);
            try {
                year = Integer.parseInt(scanner.nextLine().trim());
                if (year >= 1450 && year <= currentYear) break;
                System.out.printf("Ошибка: год должен быть в интервале 1450–%d.%n", currentYear);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное целое число.");
            }
        }

        double price;
        while (true) {
            System.out.print("Введите цену (руб.): ");
            try {
                price = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
                if (price >= 0) break;
                System.out.println("Ошибка: цена не может быть отрицательной.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное числовое значение.");
            }
        }

        bookList.add(new Book(title, author, year, price));
        System.out.println("Успешно: книга добавлена!");
    }

    public void loadFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Ошибка: указанный файл не найден (" + filePath + ").");
            return;
        }

        int addedCount = 0;
        int lineNum = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 4) {
                    System.out.printf("Предупреждение: строка %d пропущена (неверный формат).%n", lineNum);
                    continue;
                }

                try {
                    String title = parts[0].trim();
                    String author = parts[1].trim();
                    int year = Integer.parseInt(parts[2].trim());
                    double price = Double.parseDouble(parts[3].trim().replace(',', '.'));

                    if (title.isEmpty() || author.isEmpty() || year < 1450 || year > Year.now().getValue() || price < 0) {
                        System.out.printf("Предупреждение: строка %d содержит некорректные параметры.%n", lineNum);
                        continue;
                    }

                    bookList.add(new Book(title, author, year, price));
                    addedCount++;
                } catch (NumberFormatException e) {
                    System.out.printf("Ошибка парсинга в строке %d: %s%n", lineNum, e.getMessage());
                }
            }
            System.out.printf("Успешно: загружено %d книг из файла.%n", addedCount);
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    public void displayAll() {
        if (bookList.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }
        System.out.println("\n--- Все книги в коллекции (" + bookList.size() + ") ---");
        for (int i = 0; i < bookList.size(); i++) {
            System.out.printf("%d. %s%n", (i + 1), bookList.get(i));
        }
    }

    public void removeBookByIndex(Scanner scanner) {
        if (bookList.isEmpty()) {
            System.out.println("Список пуст, удалять нечего.");
            return;
        }
        displayAll();
        System.out.print("Введите номер книги для удаления (1.." + bookList.size() + "): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            Book removed = bookList.remove(idx);
            System.out.println("Удалено: " + removed.getTitle());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("Ошибка: введен неверный номер элемента.");
        }
    }
    // Обработка Stream API
    public void processStreamInteractive(Scanner scanner) {
        if (bookList.isEmpty()) {
            System.out.println("Коллекция пуста. Сначала добавьте или загрузите данные.");
            return;
        }

        System.out.println("\n=== ОБРАБОТКА ДАННЫХ (STREAM API) ===");
        System.out.println("1. Год издания (фильтр по году + сортировка по возрастанию/убыванию)");
        System.out.println("2. Цена (фильтр по цене + сортировка по возрастанию/убыванию)");
        System.out.println("3. Сортировка по алфавиту: по названию книги");
        System.out.println("4. Сортировка по алфавиту: по фамилии автора");
        System.out.print("Выберите пункт: ");
        int choice = readInt(scanner, 1, 4);

        Predicate<Book> filterPredicate = b -> true;
        Comparator<Book> comparator = Comparator.comparing(Book::getTitle);

        switch (choice) {
            case 1 -> { // Год издания
                System.out.println("\n--- Фильтрация по году издания ---");
                System.out.println("1 - Раньше указанного года (<)");
                System.out.println("2 - Точное совпадение (==)");
                System.out.println("3 - Позже указанного года (>)");
                System.out.println("4 - Все годы (без фильтра)");
                System.out.print("Выберите условие: ");
                int condition = readInt(scanner, 1, 4);

                if (condition != 4) {
                    System.out.print("Введите год: ");
                    int targetYear = readInt(scanner, 1000, 2100);
                    filterPredicate = switch (condition) {
                        case 1 -> b -> b.getYear() < targetYear;
                        case 2 -> b -> b.getYear() == targetYear;
                        case 3 -> b -> b.getYear() > targetYear;
                        default -> b -> true;
                    };
                }

                System.out.println("\nНаправление сортировки по году:");
                System.out.println("1 - По возрастанию");
                System.out.println("2 - По убыванию");
                System.out.print("Ваш выбор: ");
                int sortOrder = readInt(scanner, 1, 2);

                comparator = Comparator.comparingInt(Book::getYear);
                if (sortOrder == 2) {
                    comparator = comparator.reversed();
                }
            }

            case 2 -> { // Цена
                System.out.println("\n--- Фильтрация по цене ---");
                System.out.println("1 - Дешевле указанной суммы (<)");
                System.out.println("2 - Точная цена (==)");
                System.out.println("3 - Дороже указанной суммы (>)");
                System.out.println("4 - Любая цена (без фильтра)");
                System.out.print("Выберите условие: ");
                int condition = readInt(scanner, 1, 4);

                if (condition != 4) {
                    System.out.print("Введите сумму (руб.): ");
                    double targetPrice = readDouble(scanner, 0, 10_000_000);
                    filterPredicate = switch (condition) {
                        case 1 -> b -> b.getPrice() < targetPrice;
                        case 2 -> b -> Math.abs(b.getPrice() - targetPrice) < 0.001;
                        case 3 -> b -> b.getPrice() > targetPrice;
                        default -> b -> true;
                    };
                }

                System.out.println("\nНаправление сортировки по цене:");
                System.out.println("1 - По возрастанию");
                System.out.println("2 - По убыванию");
                System.out.print("Ваш выбор: ");
                int sortOrder = readInt(scanner, 1, 2);

                comparator = Comparator.comparingDouble(Book::getPrice);
                if (sortOrder == 2) {
                    comparator = comparator.reversed();
                }
            }

            case 3 -> { // Название книги
                System.out.println("\nНаправление сортировки по названию:");
                System.out.println("1 - От А до Я (по возрастанию)");
                System.out.println("2 - От Я до А (по убыванию)");
                System.out.print("Ваш выбор: ");
                int sortOrder = readInt(scanner, 1, 2);

                comparator = Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
                if (sortOrder == 2) {
                    comparator = comparator.reversed();
                }
            }

            case 4 -> { // Фамилия автора
                System.out.println("\nНаправление сортировки по фамилии автора:");
                System.out.println("1 - От А до Я (по возрастанию)");
                System.out.println("2 - От Я до А (по убыванию)");
                System.out.print("Ваш выбор: ");
                int sortOrder = readInt(scanner, 1, 2);

                comparator = Comparator.comparing(b -> extractLastName(b.getAuthor()), String.CASE_INSENSITIVE_ORDER);
                if (sortOrder == 2) {
                    comparator = comparator.reversed();
                }
            }
        }

        System.out.println("\n--- РЕЗУЛЬТАТ ОБРАБОТКИ ---");
        long count = bookList.stream()
                .filter(filterPredicate)
                .sorted(comparator)
                .map(b -> String.format(
                        "-> «%s» | Автор: %s | Год: %d | Цена: %.2f руб.",
                        b.getTitle(), b.getAuthor(), b.getYear(), b.getPrice()
                ))
                .peek(System.out::println)
                .count();

        if (count == 0) {
            System.out.println("Записей, удовлетворяющих заданным условиям, не найдено.");
        } else {
            System.out.printf("Всего отобрано записей: %d шт.%n", count);
        }
    }

    private String extractLastName(String author) {
        if (author == null || author.isBlank()) {
            return "";
        }
        String[] tokens = author.trim().split("\\s+");
        return tokens[tokens.length - 1];
    }

    private int readInt(Scanner scanner, int min, int max) {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.printf("Ошибка: введите число от %d до %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Ошибка: введите целое число от %d до %d: ", min, max);
            }
        }
    }

    private double readDouble(Scanner scanner, double min, double max) {
        while (true) {
            String line = scanner.nextLine().trim().replace(',', '.');
            try {
                double val = Double.parseDouble(line);
                if (val >= min && val <= max) return val;
                System.out.printf("Ошибка: введите число от %.2f до %.2f: ", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Ошибка: введите корректное число от %.2f до %.2f: ", min, max);
            }
        }
    }
}