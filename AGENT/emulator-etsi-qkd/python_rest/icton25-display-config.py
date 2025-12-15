from rich.console import Console
from rich.panel import Panel
from rich.text import Text

console = Console()

def show_interfaces():
    content = Text()
    content.append("🧩 INTERFACES:\n\n", style="bold yellow")

    content.append("🟢 Interface ID 1:\n", style="green")
    content.append("  • Device:         local-device\n", style="cyan")
    content.append("  • Port:           1\n")
    content.append("  • Role Support:   TRANSCEIVER\n", style="magenta")
    content.append("  • Type:           N/A\n")
    content.append("  • Wavelength:     1–20\n")
    content.append("  • Absorption:     0\n\n")

    content.append("🟢 Interface ID 2:\n", style="green")
    content.append("  • Device:         local-device\n", style="cyan")
    content.append("  • Port:           2\n")
    content.append("  • Role Support:   TRANSCEIVER\n", style="magenta")
    content.append("  • Type:           CV-QKD\n")
    content.append("  • Wavelength:     1–30\n")
    content.append("  • Absorption:     0\n")

    console.print(Panel(content, title="INTERFACE DETAILS", border_style="bright_blue"))

def show_link():
    content = Text()
    content.append("🔗 LINK:\n\n", style="bold yellow")

    content.append("🟠 Link ID: f81d4fae-aaaa-aaaa-aaaa-00a0c91e6bf6\n", style="orange3")
    content.append("  • Status:         ACTIVE\n", style="green")
    content.append("  • Auth Status:    PROGRESSING\n", style="yellow")
    content.append("  • Enabled:        ❌ false\n", style="red")
    content.append("  • Auth Sign:      1000101011001\n")
    content.append("  • Type:           PHYS\n")
    content.append("  • Channel Att:    3.1 dB\n")
    content.append("  • Wavelength:     57 nm\n")
    content.append("  • QKD Role:       TRANSMITTER\n\n")

    content.append("  🔵 Local:\n", style="blue")
    content.append("    • Node ID:      f81d4fae-7dec-11d0-a765-aaaaaaaaaaaa\n")
    content.append("    • Interface ID: 1\n\n")

    content.append("  🔴 Remote:\n", style="red")
    content.append("    • Node ID:      f81d4fae-7dec-11d0-a765-bbbbbbbbbbbb\n")
    content.append("    • Interface ID: 1\n")

    console.print(Panel(content, title="LINK STATUS", border_style="bright_magenta"))

if __name__ == "__main__":
    show_interfaces()
    show_link()
