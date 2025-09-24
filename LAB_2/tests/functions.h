#pragma once
#include <windows.h>
#include <string> 

extern int* numbers;
extern int size;
extern int minValue;
extern int maxValue;
extern double averageValue;

bool isStrictInteger(const std::string& str); 

DWORD WINAPI min_max(LPVOID lpParam);
DWORD WINAPI average(LPVOID lpParam);
