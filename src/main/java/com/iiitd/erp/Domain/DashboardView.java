package com.iiitd.erp.Domain;

public record DashboardView(long students,
                            long instructors,
                            long admins,
                            long courses,
                            long departments,
                            long programs,
                            long rooms,
                            long activeSections,
                            long fullSections,
                            long activeEnrollments,
                            long lockedAccounts,
                            long pendingPasswordChanges,
                            boolean maintenanceOn) {}
