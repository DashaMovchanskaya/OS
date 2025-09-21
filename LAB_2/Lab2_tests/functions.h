#pragma once
#include <windows.h>

extern int* numbers;
extern int size;
extern int minValue;
extern int maxValue;
extern double averageValue;

DWORD WINAPI min_max(LPVOID lpParam);
DWORD WINAPI average(LPVOID lpParam);
