#pragma once
#include <vector>
#include <mutex>
#include <condition_variable>
#include <thread>
#include <chrono>
#include <iostream>
extern int sizeT;
extern int* arrT;
extern std::mutex arrMutexT;
extern std::mutex coutMutexT;
extern std::condition_variable startCVT;
extern std::mutex startMutexT;
extern bool startSignalT;
extern std::vector<std::unique_ptr<std::mutex>> threadMutexesT;
extern std::vector<std::unique_ptr<std::condition_variable>> threadCVsT;

extern std::vector<bool> threadSignalsT;
extern std::vector<bool> threadTerminateT;
extern std::vector<bool> threadsFinishedT;

extern std::condition_variable allSignaledCVT;
extern std::mutex allSignaledMutexT;

void testMarker(int threadIndex);
void testMarker(int threadIndex);
bool tryMark(int* arr, int size, int index, int threadId);
void resetMarks(std::vector<int>& indices, int* arr, int size);
bool allFinished(const std::vector<bool>& flags);
