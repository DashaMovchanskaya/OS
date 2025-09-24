#include <iostream>
#include <string>
#include <sstream>
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

bool isInteger(const std::string& str) {
    std::istringstream iss(str);
    int x;
    char c;
    return (iss >> x) && !(iss >> c); 
}

int main()
{
    //setlocale(LC_ALL, "rus");
    std::string input;

    std::cout << "Enter size of array: ";
    std::cin >> input;

    if (!isInteger(input)) {
        std::cerr << "Size of array must be a number." << std::endl;
        return 1;
    } 

    size = stoi(input);

    numbers = new int[size];
    std::cout << "Enter the array elements: ";
    for (int i = 0; i < size; i++) {
        while (true) {
            std::cin >> input;
            if (!isInteger(input)) {
                std::cout << "Invalid integer: " << std::endl;
            }
            else {
                numbers[i] = stoi(input);
                break;
            }
        }
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
