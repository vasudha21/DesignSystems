import java.util.*;

enum SlotSize{
    SMALL, MEDIUM, LARGE;

    public boolean fits(Package pkg){
        switch(this){
            case SMALL: return pkg.getSize()==SMALL;
            case MEDIUM: return pkg.getSize()==SMALL || pkg.getSize()==MEDIUM;
            case LARGE: return true;
        }
        return false;
    }   
}

class Locker{
    String id;
    String location;
    List<Slot> slots;
    boolean isOnline;
    long lastheartbeat;
    public List<Slot> getSlots() {
        return slots;
    }
}

class Slot{
    String id;
    SlotSize size;
    boolean isAvailable;
    long lastHeartBeat;
    public boolean isAvailable() {
        return this.isAvailable;
    }
    public void setOccupied(boolean b) {
        this.isAvailable=b;
    }
    public SlotSize getSize() {
        return this.size;
    }
}

class Package{
    String id;
    SlotSize size;
    String assignedLockerId;
    String assignedSlotId;
    String userId;
    String otp;
    boolean isPickedUp;

    public SlotSize getSize(){
        return size;
    }
}

//===Repository===

class LockerRepository{
    Map<String, Locker> lockerDb = new HashMap<>();

    public List<Locker> findNearByLocker(String location){
        return lockerDb.values().stream().filter(l -> l.location.equals(location) && l.isOnline).collect(Collectors.toList());
    }

    public Locker findById(String id){
        return lockerDb.get(id);
    }

    public void save(Locker locker){
        lockerDb.put(locker.id, locker);
    }

}

class PackageRepository{
    Map<String, Package> packageDb = new HashMap<>();
    
    public void save(Package pkg){
        packageDb.put(pkg.id, pkg);
    }

    public Package findById(String id){
        return packageDb.get(id);
    }
}

//===interface/utilities===

interface slotAssignmentStrategy{
    Slot assignSlot(List<Locker> lockers, Package pkg);
}

public class NearestAvailableSlotStrategy implements slotAssignmentStrategy{
    public Slot assignSlot(List<Locker> lockers, Package pkg){
        for(Locker locker: lockers){
            for(Slot slot: locker.getSlots()){
                if(slot.isAvailable() && slot.getSize().fits(pkg)) return slot;
            }
        }
        return null;
    }
}
class OTPGenerator{
    public String generate(){
        return String.valueOf((int) (100000 + Math.random()*900000));
    }
}


class NotificationService{
    public void sendOTP(String userId, String otp){
        System.out.println("sending otp"+ otp + "to user" + userId);
    }
}

class LockerProxy{
    public void unlock(String lockerId, String slotId){
        System.out.println("Unlocking locker" + lockerId + "slot" + slotId);
    }
}

//===Service===

class LockerService{
    LockerRepository lockerRepository;
    PackageRepository packageRepository;
    OTPGenerator otpGen;
    NotificationService notificationService;
    private final slotAssignmentStrategy slotAssignmentStrategy;

    public LockerService(slotAssignmentStrategy slotAssignmentStrategy){
        this.slotAssignmentStrategy=slotAssignmentStrategy;
    }


    public boolean assignPackageToLocker(Package pkg, String location){
        List<Locker> lockers = lockerRepository.findNearByLocker(location);
        Slot slot = slotAssignmentStrategy.assignSlot(lockers, pkg);
        if(slot!=null){
            slot.isAvailable=false;
                slot.setOccupied(true);
                pkg.assignedLockerId=locker.id;
                pkg.assignedSlotId=locker.id;
                pkg.otp = otpGen.generate();
                packageRepository.save(pkg);
                notificationService.sendOTP(pkg.userId, pkg.otp);
                return true;
        }
        return false;
    }
}

class UserService{

    PackageRepository packageRepository;
    LockerProxy lockerProxy;
    Slot slot;

    public boolean packagePickUp(String packageId, String otp){

        Package pkg = packageRepository.findById(packageId);
        if(pkg!=null && pkg.otp==otp && !pkg.isPickedUp){
            pkg.isPickedUp=true;
            slot.isAvailable=true;
            lockerProxy.unlock(packageId, otp);
            packageRepository.save(pkg);
            return true;
        }
        return false;

    }
}

