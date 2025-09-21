#include "pch.h"  
#include "functions.h"

int* numbers = nullptr;
int size = 0;
int minValue = 0;
int maxValue = 0;
double averageValue = 0.0;

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

    return 0;
}


DWORD WINAPI average(LPVOID lpParameters) {
    double sum = 0.0;

    for (int i = 0; i < size; i++) {
        sum += numbers[i];

        Sleep(12);
    }

    averageValue = sum / size;

    return 0;
}
