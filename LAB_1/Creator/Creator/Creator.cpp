
#include <iostream>
#include <fstream>
#include <string>
#include <cstdlib>
#include "employee.h"

int main(int argc, char* argv[])
{
    setlocale(LC_ALL, "rus");

    if (argc != 3) {
        std::cerr << "Usage: Creator <filename> <record_count>\n";
        return 1;
    }

    const char* filename = argv[1];
    int count = std::atoi(argv[2]);

    std::ofstream fout(filename, std::ios::binary);
    if (!fout) {
        std::cerr << "Ошибка при открытии файла для записи.\n";
        return 1;
    }

    for (int i = 1; i <= count; ++i) {
        employee emp;
        std::cout << "Введите данные для сотрудника #" << i << ":\n";

        std::cout << "Введите номер: ";
        std::cin >> emp.num;

        std::cin.ignore(); // очистка буфера после предыдущего ввода
        std::cout << "Введите имя (до 9 символов, можно с пробелами): ";
        std::cin.getline(emp.name, sizeof(emp.name));

        std::cout << "Введите часы: ";
        std::cin >> emp.hours;

        fout.write(reinterpret_cast<char*>(&emp), sizeof(emp));
    }

    fout.close();
    std::cout << "Файл " << filename << "успешно создан!" << "\n";
    return 0;

}

