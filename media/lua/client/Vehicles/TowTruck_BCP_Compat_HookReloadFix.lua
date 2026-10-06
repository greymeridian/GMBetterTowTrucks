--[[
    TowTruck_BCP_Compat_HookReloadFix.lua

    Compatibility reload fix for Harry's Tow Truck and Better Car Physics.

    This file overrides the tow-truck post-spawn hook restore path from the
    compatibility mod only. It does not modify the original Tow Truck Workshop
    files. When a towed vehicle is restored after loading a save, the hook height
    state is normalized to prevent the saved visual hook offset from desyncing
    from the restored physics constraint.

    Author: Scott Clam
    Created: 2026-05-13
]]

if not TowTruckMod or not TowTruckMod.Hook then return end

local originalPostSpawnVehicle = TowTruckMod.Hook.postSpawnVehicle

function TowTruckMod.Hook.postSpawnVehicle(playerObj, vehicle)
    local towingVehicle = vehicle and vehicle:getVehicleTowedBy() or nil

    if towingVehicle then
        TowTruckMod.Hook.resetHook(towingVehicle)

        local modData = towingVehicle:getModData()
        modData.towHeight = 0
        towingVehicle:transmitModData()

        return
    end

    if originalPostSpawnVehicle then
        return originalPostSpawnVehicle(playerObj, vehicle)
    end
end
