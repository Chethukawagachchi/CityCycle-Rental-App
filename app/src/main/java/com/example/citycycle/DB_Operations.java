package com.example.citycycle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DB_Operations extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CityCycleDB";
    private static final int DATABASE_VERSION = 4; // Increment DATABASE_VERSION

    public DB_Operations(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DB", "onCreate() called"); // Add log

        // Create bike table
        sqLiteDatabase.execSQL("CREATE TABLE tblBike(" +
                "BikeID INTEGER PRIMARY KEY, " +
                "BikeType VARCHAR(20), " +
                "PricePerHour DOUBLE, " +
                "Image BLOB, " +
                "Description VARCHAR(100), " +
                "Location VARCHAR(50), " +
                "Availability VARCHAR(20))");

        // Create rental table
        sqLiteDatabase.execSQL("CREATE TABLE tblRental(" +
                "RentalID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Email VARCHAR(50), " +
                "BikeID INTEGER, " +
                "StartTime DATETIME, " +
                "EndTime DATETIME, " +
                "TotalPrice DOUBLE, " +
                "DiscountCode VARCHAR(20), " + // add this
                "DiscountPercentage INTEGER" + // add this
                ")");

        // Create user table
        sqLiteDatabase.execSQL("CREATE TABLE tblUser(Username VARCHAR(20), Mobile INTEGER, Address VARCHAR, Email VARCHAR(50) PRIMARY KEY, Password VARCHAR(20))");

        // Create location table
        sqLiteDatabase.execSQL("CREATE TABLE tblLocation(" +
                "LocationID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name VARCHAR(100), " +
                "Address VARCHAR(200), " +
                "Latitude DOUBLE, " +
                "Longitude DOUBLE, " +
                "Description VARCHAR(500), " +
                "AvailableBikes INTEGER)");

        

        // Create Discount table
        sqLiteDatabase.execSQL("CREATE TABLE tblDiscount (" +
                "DiscountID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Code VARCHAR(20) NOT NULL UNIQUE, " +
                "Percentage INTEGER NOT NULL, " +
                "ValidUntil DATE NOT NULL, " +
                "CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "CreatedBy VARCHAR(50)" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d("DB", "onUpgrade() called: oldVersion=" + oldVersion + ", newVersion=" + newVersion);
        if (oldVersion < 4) {
            sqLiteDatabase.execSQL("ALTER TABLE tblRental ADD COLUMN DiscountCode VARCHAR(20)");
            sqLiteDatabase.execSQL("ALTER TABLE tblRental ADD COLUMN DiscountPercentage INTEGER");
            sqLiteDatabase.execSQL("CREATE TABLE tblDiscount (" +
                    "DiscountID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Code VARCHAR(20) NOT NULL UNIQUE, " +
                    "Percentage INTEGER NOT NULL, " +
                    "ValidUntil DATE NOT NULL, " +
                    "CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "CreatedBy VARCHAR(50)" +
                    ")");
        }
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblBike");
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblRental");
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblUser");
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblLocation"); // Add this line
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tblDiscount");
        //onCreate(sqLiteDatabase);
    }

    // User creation method
    public void createUser(User user) throws Exception {
        SQLiteDatabase database = this.getWritableDatabase();

        String query = "SELECT * FROM tblUser WHERE Email = ?";
        Cursor cursor = database.rawQuery(query, new String[]{user.getEmail()});

        if (cursor.getCount() > 0) {
            cursor.close();
            database.close();
            throw new Exception("Email already exists");
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put("Username", user.getUsername());
        values.put("Mobile", user.getMobile());
        values.put("Address", user.getAddress());
        values.put("Email", user.getEmail());
        values.put("Password", user.getPassword());

        database.insert("tblUser", null, values);
        database.close();
    }

    public boolean loginUserWithEmail(String email, String password) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblUser WHERE Email = ? AND Password = ?", new String[]{email, password});
        boolean isLoggedIn = cursor.getCount() > 0;
        cursor.close();
        database.close();
        return isLoggedIn;
    }


    // Method to get a user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblUser WHERE Email = ?", new String[]{email});

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("Username")));
            user.setMobile(cursor.getInt(cursor.getColumnIndexOrThrow("Mobile")));
            user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("Address")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("Email")));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("Password")));
        }
        cursor.close();
        database.close();
        return user;
    }

    // Method to update an existing user record
    public void updateUser(User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Username", user.getUsername());
        values.put("Mobile", user.getMobile());
        values.put("Address", user.getAddress());
        values.put("Password", user.getPassword());

        database.update("tblUser", values, "Email = ?", new String[]{user.getEmail()});
        database.close();
    }

    public boolean updateUsers(User user) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Username", user.getUsername());
        values.put("Mobile", user.getMobile());
        values.put("Address", user.getAddress());

        int rowsAffected = database.update("tblUser", values, "Email = ?", new String[]{user.getEmail()});
        database.close();
        return rowsAffected > 0;
    }


    // Method to delete a user by email
    public void deleteUser(String email) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("tblUser", "Email = ?", new String[]{email});
        database.close();
    }


    public ArrayList<User> getAllUsers() {
        ArrayList<User> userList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblUser", null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User(
                        cursor.getString(cursor.getColumnIndexOrThrow("Username")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("Mobile")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Address")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Password"))
                );
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return userList;
    }

    public void addBike(Bike bike) throws Exception {
        SQLiteDatabase database = this.getWritableDatabase();

        String query = "SELECT * FROM tblBike WHERE BikeID = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(bike.getBikeID())});

        if (cursor.getCount() > 0) {
            cursor.close();
            database.close();
            throw new Exception("Bike ID already exists");
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put("BikeID", bike.getBikeID());
        values.put("BikeType", bike.getBikeType().toLowerCase());
        values.put("PricePerHour", bike.getPricePerHour());
        values.put("Image", bike.getImage());
        values.put("Description", bike.getDescription());
        values.put("Location", bike.getLocation());
        values.put("Availability", bike.getAvailability());

        database.insert("tblBike", null, values);
        database.close();
    }

    public Bike getBikeById(int bikeID) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblBike WHERE BikeID = ?",
                new String[]{String.valueOf(bikeID)});

        Bike bike = null;
        if (cursor.moveToFirst()) {
            bike = new Bike();
            bike.setBikeID(cursor.getInt(cursor.getColumnIndexOrThrow("BikeID")));
            bike.setBikeType(cursor.getString(cursor.getColumnIndexOrThrow("BikeType")));
            bike.setPricePerHour(cursor.getDouble(cursor.getColumnIndexOrThrow("PricePerHour")));
            bike.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow("Image")));
            bike.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("Description")));
            bike.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("Location")));
            bike.setAvailability(cursor.getString(cursor.getColumnIndexOrThrow("Availability")));
        }
        cursor.close();
        database.close();
        return bike;
    }

    public void updateBike(Bike bike) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BikeType", bike.getBikeType());
        values.put("PricePerHour", bike.getPricePerHour());
        values.put("Image", bike.getImage());
        values.put("Description", bike.getDescription());
        values.put("Location", bike.getLocation());
        values.put("Availability", bike.getAvailability());

        database.update("tblBike", values, "BikeID = ?",
                new String[]{String.valueOf(bike.getBikeID())});
        database.close();
    }

    public void deleteBike(int bikeID) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("tblBike", "BikeID = ?", new String[]{String.valueOf(bikeID)});
        database.close();
    }

    public ArrayList<Bike> getAllBikes() {
        ArrayList<Bike> bikeList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM tblBike", null);

        if (cursor.moveToFirst()) {
            do {
                Bike bike = new Bike();
                bike.setBikeID(cursor.getInt(cursor.getColumnIndexOrThrow("BikeID")));
                bike.setBikeType(cursor.getString(cursor.getColumnIndexOrThrow("BikeType")));
                bike.setPricePerHour(cursor.getDouble(cursor.getColumnIndexOrThrow("PricePerHour")));
                bike.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("Description")));
                bike.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("Location")));
                bike.setAvailability(cursor.getString(cursor.getColumnIndexOrThrow("Availability")));
                bike.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow("Image")));
                bikeList.add(bike);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return bikeList;
    }

    public void rentBike(BikeRental rental) {
        ContentValues values = new ContentValues();
        values.put("Email", rental.getEmail());
        values.put("BikeID", rental.getBikeID());
        values.put("StartTime", rental.getStartTime());
        values.put("EndTime", rental.getEndTime());
        values.put("TotalPrice", rental.getTotalPrice());
        values.put("DiscountCode", rental.getDiscountCode()); // Add this line
        values.put("DiscountPercentage", rental.getDiscountPercentage()); // Add this line

        SQLiteDatabase database = this.getWritableDatabase();
        database.insert("tblRental", null, values);
        database.close();
    }

    public boolean isBikeAvailable(int bikeID, String startTime, String endTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM tblRental WHERE BikeID = ? " +
                "AND ((StartTime <= ? AND EndTime > ?) OR " +
                "(StartTime < ? AND EndTime >= ?) OR " +
                "(StartTime >= ? AND EndTime <= ?))";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(bikeID),
                startTime, startTime,
                endTime, endTime,
                startTime, endTime
        });

        boolean isAvailable = cursor.getCount() == 0;
        cursor.close();
        db.close();
        return isAvailable;
    }

    public void updateBikeAvailability(int bikeID, String availability) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Availability", availability);

        database.update("tblBike", values, "BikeID = ?",
                new String[]{String.valueOf(bikeID)});
        database.close();
    }

    public ArrayList<BikeRental> getRentalsByEmail(String email) {
        ArrayList<BikeRental> rentalsList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query("tblRental",
                null, "Email = ?", new String[]{email}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int rentalID = cursor.getInt(cursor.getColumnIndexOrThrow("RentalID"));
                String emailFromDb = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
                int bikeID = cursor.getInt(cursor.getColumnIndexOrThrow("BikeID"));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("StartTime"));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow("EndTime"));
                double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("TotalPrice"));

                BikeRental rental = new BikeRental(rentalID, emailFromDb, bikeID, startTime,
                        endTime, totalPrice);
                rentalsList.add(rental);
            }
            cursor.close();
        }

        database.close();
        return rentalsList;
    }


    // Location Operations
    public void addLocation(Location location) throws Exception {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", location.getName());
        values.put("Address", location.getAddress());
        values.put("Latitude", location.getLatitude());
        values.put("Longitude", location.getLongitude());
        values.put("Description", location.getDescription());
        values.put("AvailableBikes", location.getAvailableBikes());

        long result = database.insert("tblLocation", null, values);
        database.close();

        if (result == -1) {
            throw new Exception("Failed to add location");
        }
    }

    public Location getLocationById(int locationId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query("tblLocation", null,
                "LocationID = ?", new String[]{String.valueOf(locationId)},
                null, null, null);

        Location location = null;
        if (cursor.moveToFirst()) {
            location = new Location(
                    cursor.getInt(cursor.getColumnIndexOrThrow("LocationID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Address")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("Latitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("Longitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("AvailableBikes"))
            );
        }
        cursor.close();
        database.close();
        return location;
    }

    public ArrayList<Location> getAllLocations() {
        ArrayList<Location> locations = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query("tblLocation", null,
                null, null, null, null, "Name ASC");

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location(
                        cursor.getInt(cursor.getColumnIndexOrThrow("LocationID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Address")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Latitude")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Longitude")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("AvailableBikes"))
                );
                locations.add(location);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return locations;
    }

    public boolean updateLocation(Location location) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Name", location.getName());
        values.put("Address", location.getAddress());
        values.put("Latitude", location.getLatitude());
        values.put("Longitude", location.getLongitude());
        values.put("Description", location.getDescription());
        values.put("AvailableBikes", location.getAvailableBikes());

        int rowsAffected = database.update("tblLocation", values,
                "LocationID = ?", new String[]{String.valueOf(location.getId())});
        database.close();

        return rowsAffected > 0;
    }

    public boolean deleteLocation(int locationId) {
        SQLiteDatabase database = this.getWritableDatabase();
        int rowsAffected = database.delete("tblLocation",
                "LocationID = ?", new String[]{String.valueOf(locationId)});
        database.close();

        return rowsAffected > 0;
    }

    public void updateAvailableBikes(int locationId, int newCount) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("AvailableBikes", newCount);

        database.update("tblLocation", values,
                "LocationID = ?", new String[]{String.valueOf(locationId)});
        database.close();
    }

    // Method to get locations with available bikes
    public ArrayList<Location> getAvailableLocations() {
        ArrayList<Location> locations = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query("tblLocation", null,
                "AvailableBikes > 0", null, null, null, "Name ASC");

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location(
                        cursor.getInt(cursor.getColumnIndexOrThrow("LocationID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Address")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Latitude")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("Longitude")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Description")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("AvailableBikes"))
                );
                locations.add(location);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return locations;
    }


    public void insertSampleLocations() {
        try {
            addLocation(new Location(0, "Colombo City Center",
                    "137 Sir James Peiris Mawatha, Colombo",
                    6.9271, 79.8612,
                    "Main shopping complex location", 10));

            addLocation(new Location(0, "Galle Face Green",
                    "2 Galle Road, Colombo",
                    6.9271, 79.8425,
                    "Beachside location with great views", 15));

            addLocation(new Location(0, "Independence Square",
                    "Independence Square, Colombo 7",
                    6.9101, 79.8683,
                    "Historic location with exercise areas", 12));

            addLocation(new Location(0, "Viharamahadevi Park",
                    "Viharamahadevi Park, Colombo 7",
                    6.9156, 79.8636,
                    "Central park location", 8));
        } catch (Exception e) {
            Log.e("DB", "Error inserting sample locations: " + e.getMessage());
        }
    }

    public ArrayList<BikeRental> getAllRentals() {
        ArrayList<BikeRental> rentals = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query("tblRental", null,
                null, null, null, null, "StartTime DESC");

        if (cursor.moveToFirst()) {
            do {
                BikeRental rental = new BikeRental(
                        cursor.getInt(cursor.getColumnIndexOrThrow("RentalID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Email")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("BikeID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("StartTime")),
                        cursor.getString(cursor.getColumnIndexOrThrow("EndTime")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("TotalPrice"))
                );
                rentals.add(rental);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return rentals;
    }

    // Discount Operations
    public List<Discount> getAllDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    "tblDiscount",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "ValidUntil DESC"  // Sort by validity date
            );

            if (cursor.moveToFirst()) {
                do {
                    Discount discount = new Discount(
                            cursor.getInt(cursor.getColumnIndexOrThrow("DiscountID")),
                            cursor.getString(cursor.getColumnIndexOrThrow("Code")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("Percentage")),
                            cursor.getString(cursor.getColumnIndexOrThrow("ValidUntil")),
                            cursor.getString(cursor.getColumnIndexOrThrow("CreatedAt")),
                            cursor.getString(cursor.getColumnIndexOrThrow("CreatedBy"))
                    );
                    discounts.add(discount);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_Operations", "Error getting discounts: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return discounts;
    }

    public long addDiscount(Discount discount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Code", discount.getCode());
        values.put("Percentage", discount.getPercentage());
        values.put("ValidUntil", discount.getValidUntil());
        values.put("CreatedAt", discount.getCreatedAt());
        values.put("CreatedBy", discount.getCreatedBy());

        long result = -1;
        try {
            result = db.insertOrThrow("tblDiscount", null, values);
        } catch (Exception e) {
            Log.e("DB_Operations", "Error adding discount: " + e.getMessage());
        } finally {
            db.close();
        }
        return result;
    }

    public boolean updateDiscount(Discount discount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("Code", discount.getCode());
        values.put("Percentage", discount.getPercentage());
        values.put("ValidUntil", discount.getValidUntil());

        int result = 0;
        try {
            result = db.update("tblDiscount", values,
                    "DiscountID = ?",
                    new String[]{String.valueOf(discount.getId())});
        } catch (Exception e) {
            Log.e("DB_Operations", "Error updating discount: " + e.getMessage());
        } finally {
            db.close();
        }
        return result > 0;
    }

    public boolean deleteDiscount(int discountId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        try {
            result = db.delete("tblDiscount",
                    "DiscountID = ?",
                    new String[]{String.valueOf(discountId)});
        } catch (Exception e) {
            Log.e("DB_Operations", "Error deleting discount: " + e.getMessage());
        } finally {
            db.close();
        }
        return result > 0;
    }


    public Discount getDiscountByCode(String discountCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Discount discount = null;

        try {
            cursor = db.query(
                    "tblDiscount",
                    null,
                    "Code = ?",
                    new String[]{discountCode},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                discount = new Discount(
                        cursor.getInt(cursor.getColumnIndexOrThrow("DiscountID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("Code")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("Percentage")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ValidUntil")),
                        cursor.getString(cursor.getColumnIndexOrThrow("CreatedAt")),
                        cursor.getString(cursor.getColumnIndexOrThrow("CreatedBy"))
                );
            }
        } catch (Exception e) {
            Log.e("DB_Operations", "Error getting discount by code: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return discount;
    }

    public void updateExpiredRentals() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get current datetime
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = "2025-03-21 06:58:42"; // Use the current datetime

        // Update bikes where rental period has expired
        String query = "UPDATE Bikes SET Availability = 'Available' " +
                "WHERE BikeID IN (SELECT BikeID FROM Rentals " +
                "WHERE EndDateTime < ? AND Status = 'Active')";

        db.execSQL(query, new String[]{currentDateTime});

        // Update rental status
        query = "UPDATE Rentals SET Status = 'Completed' " +
                "WHERE EndDateTime < ? AND Status = 'Active'";

        db.execSQL(query, new String[]{currentDateTime});

        db.close();
    }

    public boolean isConnected() {
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
            return db != null && db.isOpen();
        } catch (Exception e) {
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}