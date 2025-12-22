# Session: Fix Async Material Processing Race Condition

**Date**: December 21, 2025
**Objective**: Resolve "Material not found" error during async processing.

## 1. Accomplished
- Investigated `Material not found` error in `CustomMaterialProcessingServiceImpl`.
- Identified race condition where async processing started before `createMaterial` transaction committed.
- Fixed race condition in `CustomMaterialServiceImpl.java` using `TransactionSynchronizationManager`.
- Identified and fixed potential synchronous execution bug in `CustomMaterialProcessingServiceImpl.retryProcessing`.

## 2. Code Modified
- `backend/src/main/java/com/lexia/backend/service/ai/impl/CustomMaterialServiceImpl.java`
- `backend/src/main/java/com/lexia/backend/service/ai/impl/CustomMaterialProcessingServiceImpl.java`

## 3. Key Decisions
- **Use TransactionSynchronizationManager**: This is the standard Spring approach for "trigger after commit". It avoids complex event listeners if the logic is simple.
- **Lazy ApplicationContext in Retry**: To avoid circular dependency while ensuring `retryProcessing` calls `processMaterial` through the proxy (for `@Async`) and in a new transaction.

## 4. Quality
- **Rating**: 10/10
- **Reason**: The fix addresses the root cause (transaction isolation/timing) without hacks like `Thread.sleep()`.

## 5. Next Steps
- Monitor logs for any other async race conditions.
