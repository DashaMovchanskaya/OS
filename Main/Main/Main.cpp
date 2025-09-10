
#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>
#include <cstring>
#include <windows.h>
#include "employee.h"

void runProcess(const std::string& command) {
    STARTUPINFOA si;
    PROCESS_INFORMATION piApp; 

    ZeroMemory(&si, sizeof(STARTUPINFO));
    si.cb = sizeof(STARTUPINFO);

    char commandLine[512];
    strncpy_s(commandLine, command.c_str(), sizeof(commandLine));
    commandLine[sizeof(commandLine) - 1] = '\0';

    if (!CreateProcessA(NULL, commandLine, NULL, NULL, FALSE, 0, NULL, NULL, &si, &piApp)) {
        std::cerr << "Ошибка запуска: " << commandLine << "\n";
        return;
    }

    WaitForSingleObject(piApp.hProcess, INFINITE);
    CloseHandle(piApp.hThread);
    CloseHandle(piApp.hProcess);
}

int main()
{
    setlocale(LC_ALL, "rus");

    std::cout << "Введите имя исходного бинарного файла: ";
    std::string binfileName;
    std::cin >> binfileName;

    std::cout << "Введите количество записей в этом файле: ";
    int recordCount;
    std::cin >> recordCount;

    std::string creatorCommand = "Creator.exe " + binfileName + " " + std::to_string(recordCount);
    runProcess(creatorCommand);

    std::ifstream bfin(binfileName, std::ios::binary);
    if (!bfin) {
        std::cerr << "Ошибка при открытии бинарного файла.\n";
        return 0;
    }

    employee emp;
    std::cout << "\nСодержимое бинарного файла:\n";
    while (bfin.read(reinterpret_cast<char*>(&emp), sizeof(emp))) {
        std::cout << "Номер: " << emp.num<< ", Имя: " << emp.name << ", Часы: " << std::fixed << std::setprecision(2) << emp.hours << "\n";
    }
    bfin.close();

    std::cout << "Введите имя файла отчета: ";
    std::string reportfileName;
    std::cin >> reportfileName;

    std::cout << "Введите оплату за час работы: ";
    double payment;
    std::cin >> payment;

    std::string reporterCommand = "Reporter.exe " + binfileName + " " + reportfileName + " " + std::to_string(payment);
    runProcess(reporterCommand);

    std::ifstream tfin(reportfileName);
    if (!tfin) {
        std::cerr << "Ошибка при открытии файла отчета.\n";
        return 0;
    }

    std::cout << "\nОтчет:\n";
    std::string line;
    while (std::getline(tfin, line)) {
        std::cout << line << "\n";
    }
    tfin.close();

    std::cout << "\nРабота завершена.\n";
    return 0;
}


