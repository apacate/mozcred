function formatarValor(valor) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(valor);
}

async function calcularAmortizacao() {
    if (!validarFormulario()) {
        return;
    }

    const loanDto = {
        amount: parseFloat(document.getElementById('amount').value),
        date: document.getElementById('date').value,
        interestRate: parseFloat(document.getElementById('interestRateSelect').value),
        amortizationType: document.getElementById('amortizationType').value,
        frequency: document.getElementById('frequency').value,
        numberOfInstallments: parseInt(document.getElementById('numberOfInstallments').value),
        clientId: document.getElementById('clientId').value
    };

    const resultadoDiv = document.getElementById('resultado');
    resultadoDiv.innerHTML = '<p class="text-warning"><i class="fas fa-spinner fa-spin"></i> Calculando...</p>';

    try {
        const response = await fetch('/loans/calcular-amortizacao', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loanDto)
        });

        if (!response.ok) {
            throw new Error('Falha ao calcular o plano de amortização.');
        }

        const data = await response.json();
        renderizarPlanoDeAmortizacao(data);
    } catch (error) {
        console.error('Erro ao calcular amortização:', error);
        resultadoDiv.innerHTML = '<p class="text-danger">Erro ao calcular o plano de amortização. Tente novamente mais tarde.</p>';
    }
}

function renderizarPlanoDeAmortizacao(data) {
    const resultadoDiv = document.getElementById('resultado');
    resultadoDiv.innerHTML = '';

    if (!data || !Array.isArray(data)) {
        resultadoDiv.innerHTML = '<p class="text-danger">Nenhum resultado encontrado. Verifique os dados informados e tente novamente.</p>';
        return;
    }

    let resultado = '<h3>Plano de Amortização</h3>';
    resultado += '<table class="table table-bordered">';
    resultado += '<thead><tr><th>Número</th><th>Data</th><th>Amortizado</th><th>Juros</th><th>Total</th></tr></thead>';
    resultado += '<tbody>';

    let totalJuros = 0;
    let totalAcumulado = 0;

    data.forEach(installment => {
        totalJuros += installment.installmentInterest;
        totalAcumulado += installment.installmentTotal;

        resultado += `
            <tr>
                <td>${installment.installmentNumber}</td>
                <td>${new Date(installment.installmentDate).toLocaleDateString()}</td>
                <td>${formatarValor(installment.installmentAmount)}</td>
                <td>${formatarValor(installment.installmentInterest)}</td>
                <td>${formatarValor(installment.installmentTotal)}</td>
            </tr>
        `;
    });

    resultado += '</tbody>';
    resultado += '</table>';

    resultado += `
        <p><strong>Total de Juros:</strong> ${formatarValor(totalJuros)}</p>
        <p><strong>Total Acumulado:</strong> ${formatarValor(totalAcumulado)}</p>
    `;

    resultadoDiv.innerHTML = resultado;
}

function validarFormulario() {
    const amount = parseFloat(document.getElementById('amount').value);
    const date = document.getElementById('date').value;
    const numInstallments = parseInt(document.getElementById('numberOfInstallments').value);
    const interestRate = parseFloat(document.getElementById('interestRateSelect').value);
    const amortizationType = document.getElementById('amortizationType').value;
    const frequency = document.getElementById('frequency').value;
    const clientId = document.getElementById('clientId').value;

    if (!amount || !date || !numInstallments || !interestRate || !amortizationType || !frequency || !clientId) {
        alert("Preencha todos os campos corretamente antes de enviar.");
        return false;
    }

    if (isNaN(amount) || amount < 1) {
        alert("O valor do empréstimo deve ser maior ou igual a 1.");
        return false;
    }

    if (isNaN(numInstallments) || numInstallments < 1) {
        alert("O número de parcelas deve ser maior ou igual a 1.");
        return false;
    }

    if (!date) {
        alert("A data do empréstimo é obrigatória.");
        return false;
    }

    if (isNaN(interestRate) || interestRate <= 0) {
        alert("A taxa de juros deve ser maior que zero.");
        return false;
    }

    if (!amortizationType) {
        alert("O tipo de amortização é obrigatório.");
        return false;
    }

    if (!frequency) {
        alert("A frequência é obrigatória.");
        return false;
    }

    if (!clientId) {
        alert("Selecione um cliente válido.");
        return false;
    }

    return true;
}

document.addEventListener('DOMContentLoaded', function () {
    const dataAtual = new Date();
    const campoData = document.getElementById('date');
    campoData.value = dataAtual.toISOString().split('T')[0];
});

document.getElementById('loanForm').addEventListener('submit', function (event) {
    if (!validarFormulario()) {
        event.preventDefault();
    }
});