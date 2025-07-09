package util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import domain.Address;
import domain.Machine;
import domain.Maintenance;
import domain.Report;
import domain.Site;
import domain.User;
import dto.AddressDTO;
import dto.MachineDTO;
import dto.MaintenanceDTO;
import dto.ReportDTO;
import dto.SiteDTOWithMachines;
import dto.SiteDTOWithoutMachines;
import dto.UserDTO;

public class DTOMapper {

    public static  AddressDTO toAddressDTO(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressDTO(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getPostalcode(),
                address.getCity()
        );
    }

    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBirthdate(),
                toAddressDTO(user.getAddress()),
                user.getRole(),
                user.getStatus(),
                user.getPassword()
        );
    }

    public static SiteDTOWithoutMachines toSiteDTOWithoutMachines(Site site) {
        if (site == null) {
            return null;
        }
        return new SiteDTOWithoutMachines(
                site.getId(),
                site.getSiteName(),
                toUserDTO(site.getVerantwoordelijke()),
                site.getStatus(),
                toAddressDTO(site.getAddress())
        );
    }

    public static MachineDTO toMachineDTO(Machine machine) {
        if (machine == null) {
            return null;
        }
        return new MachineDTO(
                machine.getId(),
                toSiteDTOWithoutMachines(machine.getSite()),
                toUserDTO(machine.getTechnician()),
                machine.getCode(),
                machine.getMachineStatus(),
                machine.getProductionStatus(),
                machine.getLocation(),
                machine.getProductInfo(),
                machine.getLastMaintenance(),
                machine.getFutureMaintenance(),
                machine.getNumberDaysSinceLastMaintenance(),
                machine.getUpTimeInHours()
        );
    }

    public static Set<MachineDTO> toMachineDTOSet(Set<Machine> machines) {
        if (machines == null) {
            return new HashSet<>();
        }
        return machines.stream()
                .map(DTOMapper::toMachineDTO)
                .collect(Collectors.toSet());
    }

    public static SiteDTOWithMachines toSiteDTOWithMachines(Site site) {
        if (site == null) {
            return null;
        }
        return new SiteDTOWithMachines(
                site.getId(),
                site.getSiteName(),
                toUserDTO(site.getVerantwoordelijke()),
                toMachineDTOSet(site.getMachines()),
                site.getStatus(),
                toAddressDTO(site.getAddress())
        );
    }

    public static MaintenanceDTO toMaintenanceDTO(Maintenance maintenance) {
        if (maintenance == null) {
            return null;
        }
        return new MaintenanceDTO(
                maintenance.getId(),
                maintenance.getExecutionDate(),
                maintenance.getStartDate(),
                maintenance.getEndDate(),
                toUserDTO(maintenance.getTechnician()),
                maintenance.getReason(),
                maintenance.getComments(),
                maintenance.getStatus(),
                toMachineDTO(maintenance.getMachine())
        );
    }

    public static ReportDTO toReportDTO(Report report) {
        if (report == null) {
            return null;
        }
        return new ReportDTO(
                report.getMaintenance(),
                report.getSite(),
                report.getTechnician(),
                report.getStartDate(),
                report.getStartTime(),
                report.getEndDate(),
                report.getEndTime(),
                report.getReason(),
                report.getRemarks()
        );
    }

    public static Address toAddress(AddressDTO dto) {
        if (dto == null) {
            return null;
        }
        Address.Builder builder = new Address.Builder()
                .buildStreet(dto.street())
                .buildNumber(dto.number())
                .buildPostalcode(dto.postalcode())
                .buildCity(dto.city());
        
        try {
            Address address = builder.build();
            address.setId(dto.id());
            return address;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Address from DTO", e);
        }
    }

    public static User toUser(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Address adres = toAddress(dto.address());
        User user = new User.Builder()
                .buildFirstName(dto.firstName())
                .buildLastName(dto.lastName())
                .buildEmail(dto.email())
                .buildPhoneNumber(dto.phoneNumber())
                .buildBirthdate(dto.birDate())
                .buildAddress(adres.getStreet(), adres.getNumber(), adres.getPostalcode(), adres.getCity())
                .buildRole(dto.role())
                .buildStatus(dto.status())
                .buildPassword(dto.password())
                .build();
        
        try {
            user.setId(dto.id());
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error creating User from DTO", e);
        }
    }

    public static Site toSite(SiteDTOWithoutMachines dto) {
        if (dto == null) {
            return null;
        }
        Address adres = toAddress(dto.address());
        
        Site.Builder builder = new Site.Builder()
                .buildSiteName(dto.siteName())
                .buildAddress(adres.getStreet(), adres.getNumber(), adres.getPostalcode(), adres.getCity())
                .buildVerantwoordelijke(toUser(dto.verantwoordelijke()))
                .buildStatus(dto.status())
                .buildMachines(new HashSet<>());
        
        try {
            Site site = builder.build();
            site.setId(dto.id());
            return site;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Site from DTO", e);
        }
    }

    public static Site toSite(SiteDTOWithMachines dto) {
        if (dto == null) {
            return null;
        }
        // First create the Site without machines
        SiteDTOWithoutMachines siteWithoutMachinesDTO = new SiteDTOWithoutMachines(
                dto.id(), dto.siteName(), dto.verantwoordelijke(), 
                dto.status(), dto.address()
        );
        Site site = toSite(siteWithoutMachinesDTO);
        
        // Then add machines if available
        if (dto.machines() != null) {
            Set<Machine> machines = dto.machines().stream()
                    .map(DTOMapper::toMachine)
                    .collect(Collectors.toSet());
            
            // Update each machine with this site
            machines.forEach(machine -> {
                machine.setSite(site);
            });
        }
        
        return site;
    }
    
    public static Machine toMachine(MachineDTO dto) {
        if (dto == null) {
            return null;
        }
        Machine.Builder builder = new Machine.Builder()
                .buildCode(dto.code())
                .buildLocation(dto.location())
                .buildProductInfo(dto.productInfo())
                .buildMachineStatus(dto.machineStatus())
                .buildProductionStatus(dto.productionStatus())
                .buildFutureMaintenance(dto.futureMaintenance())
                .buildSite(toSite(dto.site()))
                .buildTechnician(toUser(dto.technician()));
        
        try {
            Machine machine = builder.build();
            machine.setId(dto.id());
            machine.setLastMaintenance(dto.lastMaintenance());
            machine.setNumberDaysSinceLastMaintenance(dto.numberDaysSinceLastMaintenance());
            return machine;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Machine from DTO", e);
        }
    }

    public static Maintenance toMaintenance(MaintenanceDTO dto) {
        if (dto == null) {
            return null;
        }
        Maintenance.Builder builder = new Maintenance.Builder()
                .buildExecutionDate(dto.executionDate())
                .buildStartDate(dto.startDate())
                .buildEndDate(dto.endDate())
                .buildTechnician(toUser(dto.technician()))
                .buildReason(dto.reason())
                .buildComments(dto.comments())
                .buildMaintenanceStatus(dto.status())
                .buildMachine(toMachine(dto.machine()));
        
        try {
            Maintenance maintenance = builder.build();
            maintenance.setId(dto.id());
            return maintenance;
        } catch (Exception e) {
            throw new RuntimeException("Error creating Maintenance from DTO", e);
        }
    }

    public static Report toReport(ReportDTO dto) {
        if (dto == null) {
            return null;
        }
        Report.Builder builder = new Report.Builder()
                .buildMaintenance(dto.maintenance())
                .buildSite(dto.site())
                .buildTechnician(dto.technician())
                .buildstartDate(dto.startDate())
                .buildStartTime(dto.startTime())
                .buildEndDate(dto.endDate())
                .buildEndTime(dto.endTime())
                .buildReason(dto.reason())
                .buildRemarks(dto.comments());
        
        try {
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error creating Report from DTO", e);
        }
    }
}