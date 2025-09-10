
#include <iostream>
#include <fstream>
#include <iomanip>
#include "employee.h"

int main(int args, char* arg[])
{
    setlocale(LC_ALL, "rus");

    if (args != 4) {
        std::cerr << "Usage: Reporter <input_binary_file> <output_report_file> <hourly_rate>\n";
        return 1;
    }

    const char* binfileName = arg[1];
    const char* reportfileName = arg[2];
    double payment = std::atof(arg[3]);

    std::ifstream fin(binfileName, std::ios::binary);
    if (!fin) {
        std::cerr << "Ошибка при открытие входного файла!\n";
        return 1;
    }

    std::ofstream fout(reportfileName);
    if (!fout) {
        std::cerr << "Ошибка при открытии файла для записис!\n";
        return 1;
    }

    fout << "Отчет по файлу " << binfileName << "\n";
    fout << std::left << std::setw(18) << "Номер сотрудника, " << std::setw(16) << "Имя сотрудника, " << std::setw(8) << "Часы, " << std::setw(8) << "Зарплата\n";

    employee emp;
    while (fin.read(reinterpret_cast<char*>(&emp), sizeof(emp))) {
        double salary = payment * emp.hours;
        fout << std::left << std::setw(18) << emp.num << std::setw(16) << emp.name << std::setw(8) << emp.hours << std::setw(8) << salary << "\n";
    }

    fin.close();
    fout.close();

    std::cout << "Файл " << reportfileName << " успешно создан!\n";
    return 0;
}


