#pragma once

#include <vector>
#include <windows.h>

bool tryMark(int* arr, int size, int index, int threadId);
void resetMarks(std::vector<int>& indices, int* arr, int size);
bool allFinished(const std::vector<bool>& flags);

extern CRITICAL_SECTION csT;
extern int* arrT;
extern int sizeT;

extern HANDLE startEvent;
extern HANDLE stopEvent;
extern HANDLE exitEvent;
extern HANDLE threadHandle;

DWORD WINAPI testMarker(LPVOID param);
