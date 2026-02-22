import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Interface for Economic Functions
 * Demonstrates abstraction for different types of market curves.
 */
interface MarketCurve {
    double getPrice(double quantity);
    double getSlope();
    double getIntercept();
    String getName();
}

/**
 * Represents a Linear Curve (P = a + bQ)
 */
class LinearMarketCurve implements MarketCurve {
    private final double intercept; // (a)
    private final double slope;     // (b)
    private final String name;

    public LinearMarketCurve(String name, double intercept, double slope) {
        this.name = name;
        this.intercept = intercept;
        this.slope = slope;
    }

    @Override public double getPrice(double q) { return intercept + (slope * q); }
    @Override public double getSlope() { return slope; }
    @Override public double getIntercept() { return intercept; }
    @Override public String getName() { return name; }
}

/**
 * Core Market Engine to calculate Equilibrium, Elasticity, and Taxation.
 */
class MarketEngine {
    private final MarketCurve demand;
    private final MarketCurve supply;

    public MarketEngine(MarketCurve demand, MarketCurve supply) {
        this.demand = demand;
        this.supply = supply;
    }

    /**
     * Solves for Equilibrium: Qd = Qs
     * Algebra: a + bQ = c + dQ + tax => Q = (c - a + tax) / (b - d)
     */
    public void runAnalysis(double tax) {
        double a = demand.getIntercept();
        double b = demand.getSlope();
        double c = supply.getIntercept();
        double d = supply.getSlope();

        // Calculate Equilibrium Quantity
        double qStar = (c - a + tax) / (b - d);
        
        if (qStar <= 0) {
            System.out.println("Market Error: No positive equilibrium found.");
            return;
        }

        // Calculate Equilibrium Price
        double pStar = demand.getPrice(qStar);

        // Calculate Price Elasticity of Demand (PED) = |(1/slope) * (P/Q)|
        double elasticity = Math.abs((1 / b) * (pStar / qStar));

        // Calculate Deadweight Loss (DWL)
        // For simplicity: 0.5 * tax * (Change in Quantity)
        double qInitial = (c - a) / (b - d);
        double dwl = 0.5 * tax * Math.abs(qInitial - qStar);

        printReport(qStar, pStar, elasticity, tax, dwl);
        exportToCSV(qStar, tax);
    }

    private void printReport(double q, double p, double ed, double tax, double dwl) {
        System.out.println("===========================================");
        System.out.println("   ECONOMIC ANALYSIS REPORT (Tax: " + tax + ")");
        System.out.println("===========================================");
        System.out.printf("Equilibrium Quantity (Q*): %.2f\n", q);
        System.out.printf("Equilibrium Price    (P*): %.2f\n", p);
        System.out.printf("Price Elasticity (PED):  %.2f (%s)\n", ed, (ed > 1 ? "Elastic" : "Inelastic"));
        if (tax > 0) {
            System.out.printf("Deadweight Loss (DWL):   %.2f\n", dwl);
        }
        System.out.println("===========================================\n");
    }

    private void exportToCSV(double qLimit, double tax) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("MarketData.csv"))) {
            writer.println("Quantity,DemandPrice,SupplyPriceWithTax");
            for (double i = 0; i <= qLimit * 1.5; i += 1) {
                writer.printf("%.2f,%.2f,%.2f\n", i, demand.getPrice(i), supply.getPrice(i) + tax);
            }
            System.out.println("[System] Data exported to MarketData.csv");
        } catch (IOException e) {
            System.out.println("Export error: " + e.getMessage());
        }
    }
}

public class MarketSimulationApp {
    public static void main(String[] args) {
        // Demand: P = 100 - 2Q | Supply: P = 10 + 3Q
        MarketCurve demand = new LinearMarketCurve("Demand", 100, -2);
        MarketCurve supply = new LinearMarketCurve("Supply", 10, 3);

        MarketEngine engine = new MarketEngine(demand, supply);

        // 1. Analyze Free Market
        engine.runAnalysis(0);

        // 2. Analyze Market with Government Tax (e.g., $10 per unit)
        engine.runAnalysis(10);
    }
}