// лабораторная работа 2.cpp : Этот файл содержит функцию "main". Здесь начинается и заканчивается выполнение программы.
//Поток main должен выполнить следующие действия:
//1. Создать массив целых чисел, размерность и элементы которого вводятся с консоли.
//2. Создать потоки min_max и average.
//3. Дождаться завершения потоков min_max и average.
//4. Заменить максимальный и минимальный элементы массива на среднее значение элементов
//массива.Вывести полученные результаты на консоль.
//5. Завершить работу.
//Поток min_max должен выполнить следующие действия :
//1. Найти минимальный и максимальный элементы массива и вывести их на консоль.После
//каждого сравнения элементов «спать» 7 миллисекунд.
//2. Завершить свою работу.
//Поток average должен выполнить следующие действия :
//1. Найти среднее арифметическое значение элементов массива и вывести его на консоль.
//После каждой операции суммирования элементов «спать» 12 миллисекунд.
//2. Завершить свою работу.

#include <iostream>
#include <windows.h>

int* numbers;
int size; 
int minValue, maxValue;
double averageValue;

DWORD WINAPI min_max(LPVOID lpParameters) {
    minValue = numbers[0];
    maxValue = numbers[0];

    for (int i = 0; i < size; i++) {
        if (numbers[i] > maxValue) {
            maxValue = numbers[i];
        }

        Sleep(7);

        if (numbers[i] < minValue) {
            minValue = numbers[i];
        }

        Sleep(7);
    }

    std::cout << "Min = " << minValue << "\nMax = " << maxValue << std::endl;

    return 0;
}


DWORD WINAPI average(LPVOID lpParameters) {
    double sum = 0.0;

    for (int i = 0; i < size; i++) {
        sum += numbers[i];

        Sleep(12);
    }

    averageValue = sum / size;
    std::cout << "Average = " << averageValue << std::endl;

    return 0;
}


int main()
{
    //setlocale(LC_ALL, "rus");

    std::cout << "Enter size of array: ";
    std::cin >> size;
    numbers = new int[size];
    std::cout << "Enter the array elements: ";
    for (int i = 0; i < size; i++) {
        std::cin >> numbers[i];
    }

    HANDLE hMin_max = CreateThread(NULL, 0, min_max, NULL, 0, NULL);
    HANDLE hAverage = CreateThread(NULL, 0, average, NULL, 0, NULL);

    if (hMin_max == NULL || hAverage == NULL) { 
        std::cerr << "Failed to create one or more threads." << std::endl;
        return 1;
    }

    WaitForSingleObject(hMin_max, INFINITE); 
    WaitForSingleObject(hAverage, INFINITE);

    for (int i = 0; i < size; i++) {
        if (numbers[i] == maxValue || numbers[i] == minValue) {
            numbers[i] = static_cast<int>(averageValue);
        }
    }

    std::cout << "Modifited array: ";
    for (int i = 0; i < size; i++) {
        std::cout << numbers[i] << " ";
    }
    std::cout << std::endl;

    CloseHandle(hMin_max); 
    CloseHandle(hAverage);

    delete[] numbers;

    return 0;
}
